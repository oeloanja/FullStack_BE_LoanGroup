package com.billit.loangroup_service.service;

import com.billit.loangroup_service.connection.loan.client.LoanServiceClient;
import com.billit.loangroup_service.connection.loan.dto.LoanRequestClientDto;
import com.billit.loangroup_service.connection.loan.dto.LoanResponseClientDto;
import com.billit.loangroup_service.dto.LoanGroupResponseDto;
import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.enums.RiskLevel;
import com.billit.loangroup_service.exception.CustomException;
import com.billit.loangroup_service.kafka.event.LoanGroupFullEvent;
import com.billit.loangroup_service.repository.LoanGroupRepository;
import com.billit.loangroup_service.utils.ValidationUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.billit.loangroup_service.exception.ErrorCode.*;

@Slf4j
@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
@EnableAsync
@EnableScheduling
@RequiredArgsConstructor
public class LoanGroupService {
    private final LoanGroupRepository loanGroupRepository;
    private final LoanServiceClient loanServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    private static final int MIN_EMPTY_GROUPS_PER_RISK = 3;
    private static final long GROUP_TIMEOUT_HOURS = 24;

    @Transactional
    public LoanGroupResponseDto assignGroup(LoanRequestClientDto request) {
        try {
            LoanResponseClientDto loanResponseClient = Optional.ofNullable(loanServiceClient.getLoanById(request.getLoanId()))
                    .orElseThrow(() -> new CustomException(LOAN_NOT_FOUND, request.getLoanId()));

            BigDecimal intRate = loanResponseClient.getIntRate();
            RiskLevel riskLevel = RiskLevel.fromInterestRate(intRate);

            Long groupId = loanGroupRepository.findAvailableGroupId(
                    riskLevel.ordinal(),
                    LoanGroup.MAX_MEMBERS
            ).orElseThrow(() -> new CustomException(LOAN_GROUP_NOT_FOUND, "배정 가능한 그룹이 없습니다. 잠시 후 다시 시도해주세요."));

            int updated = loanGroupRepository.incrementMemberCount(groupId);
            if (updated != 1) {
                throw new CustomException(INVALID_PARAMETER, "그룹 인원 조정에 실패했습니다.");
            }

            LoanGroup targetGroup = loanGroupRepository.findById(groupId)
                    .map(group -> {
                        entityManager.refresh(group);
                        return group;
                    })
                    .orElseThrow(() -> new CustomException(LOAN_GROUP_NOT_FOUND, groupId));

            if (targetGroup.getMemberCount() >= LoanGroup.MAX_MEMBERS) {
                targetGroup.updateGroupAsFull();
                loanGroupRepository.saveAndFlush(targetGroup);
                kafkaTemplate.send("loan-group-full",
                        targetGroup.getGroupId().toString(),
                        new LoanGroupFullEvent(targetGroup.getGroupId()));
            }
            return LoanGroupResponseDto.from(targetGroup);
        } catch (Exception e) {
            throw new CustomException(INVALID_PARAMETER, "예기치 못한 오류가 발생했습니다.");
        }
    }

    @Transactional
    @Async
    public void checkAndReplenishGroupPool(RiskLevel riskLevel) {
        try {
            int emptyGroups = loanGroupRepository.countByRiskLevelAndMemberCountAndIsFullFalse(
                    riskLevel.ordinal());

            int groupsToCreate = MIN_EMPTY_GROUPS_PER_RISK - emptyGroups;

            if (groupsToCreate > 0) {
                for (int i = 0; i < groupsToCreate; i++) {
                    LoanGroup newGroup = new LoanGroup(
                            generateGroupName(),
                            riskLevel,
                            LocalDateTime.now()
                    );
                    loanGroupRepository.save(newGroup);
                }
            }
        } catch (Exception e) {
            throw new CustomException(INVALID_PARAMETER,
                   "group pool 관리 중 오류 발생: riskLevel " + riskLevel);
        }
    }

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Transactional
    public void scheduledPoolCheck() {
        try {
            for (RiskLevel riskLevel : RiskLevel.values()) {
                checkAndReplenishGroupPool(riskLevel);
            }
        } catch (Exception e) {
            throw new CustomException(INVALID_PARAMETER, "group pool 체크 중 오류 발생");
        }
    }

    private String generateGroupName() {
        return "GR" + UUID.randomUUID();
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void checkAndCloseStaleGroups() {
        try {
            log.info("밤이 되었습니다. 아직 완성되지 못한 그룹은 눈을 떠주세요...");
            LocalDateTime timeoutThreshold = LocalDateTime.now().minusHours(GROUP_TIMEOUT_HOURS);
            List<LoanGroup> staleGroups = loanGroupRepository.findByCreatedAtLessThanAndIsFulledFalse(timeoutThreshold);

            for (LoanGroup group : staleGroups) {
                if (group.getMemberCount() > 0) {
                    group.updateGroupAsFull();
                    loanGroupRepository.save(group);

                    kafkaTemplate.send("loan-group-full",
                            group.getGroupId().toString(),
                            new LoanGroupFullEvent(group.getGroupId()));
                }
            }

        } catch (Exception e) {
            throw new CustomException(INVALID_PARAMETER, "오래된 그룹 처리 중 오류가 발생했습니다.");
        }
    }

    // 투자 가능한 그룹 목록 조회
    public List<LoanGroupResponseDto> getActiveGroups(RiskLevel riskLevel) {
        return loanGroupRepository.findByRiskLevelAndLoanGroupAccount_IsClosedFalse(riskLevel)
                .stream()
                .map(LoanGroupResponseDto::from)
                .collect(Collectors.toList());
    }

    // 특정 그룹 정보 조회
    public LoanGroupResponseDto getGroupDetails(Integer groupId) {
        Optional<LoanGroup> group = loanGroupRepository.findById(Long.valueOf(groupId));
        ValidationUtils.validateLoanGroupExistence(group, groupId);
        return LoanGroupResponseDto.from(group.get());
    }
}