package com.billit.loangroup_service.service;

import com.billit.loangroup_service.connection.loan.client.LoanServiceClient;
import com.billit.loangroup_service.connection.loan.dto.LoanRequestClientDto;
import com.billit.loangroup_service.connection.loan.dto.LoanResponseClientDto;
import com.billit.loangroup_service.dto.LoanGroupResponseDto;
import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.enums.RiskLevel;
import com.billit.loangroup_service.kafka.event.LoanGroupFullEvent;
import com.billit.loangroup_service.repository.LoanGroupRepository;
import com.billit.loangroup_service.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LoanGroupService {
    private final LoanGroupRepository loanGroupRepository;
    private final LoanServiceClient loanServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ApplicationEventPublisher eventPublisher;

    // 멤버 추가: LoanGroup entity 데이터가 편집됨
    @Transactional
    public LoanGroupResponseDto assignGroup(LoanRequestClientDto request) {
        LoanResponseClientDto loanResponseClient = loanServiceClient.getLoanById(request.getLoanId());
        ValidationUtils.validateLoanExistence(loanResponseClient, request.getLoanId());

        BigDecimal intRate = loanResponseClient.getIntRate();
        RiskLevel riskLevel = RiskLevel.fromInterestRate(intRate);
        List<LoanGroup> activeGroups = loanGroupRepository.findAllByRiskLevelAndIsFulledFalseOrderByMemberCountDesc(riskLevel);
        LoanGroup targetGroup;

        if (activeGroups.isEmpty() || (activeGroups.size() < 3 && LoanGroup.isAllActiveGroupsNearlyFull(activeGroups))) {
            targetGroup = new LoanGroup("GR" + UUID.randomUUID(), riskLevel, LocalDateTime.now());
            loanGroupRepository.saveAndFlush(targetGroup);
        } else {
            targetGroup = activeGroups.get(0);
        }

        ValidationUtils.validateGroupNotFull(targetGroup);

        targetGroup.incrementMemberCount();
        if (targetGroup.getMemberCount() >= LoanGroup.MAX_MEMBERS) {
            targetGroup.updateGroupAsFull();
            loanGroupRepository.saveAndFlush(targetGroup);
            kafkaTemplate.send("loan-group-full",
                    targetGroup.getGroupId().toString(),
                    new LoanGroupFullEvent(targetGroup.getGroupId()));
        }

        return LoanGroupResponseDto.from(targetGroup);
    }

//    // 투자 가능한 그룹 목록 조회
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