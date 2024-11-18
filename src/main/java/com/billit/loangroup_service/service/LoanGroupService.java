package com.billit.loangroup_service.service;

import com.billit.loangroup_service.cache.PlatformAccountCache;
import com.billit.loangroup_service.connection.client.LoanServiceClient;
import com.billit.loangroup_service.connection.dto.LoanRequestClientDto;
import com.billit.loangroup_service.connection.dto.LoanResponseClientDto;
import com.billit.loangroup_service.dto.LoanGroupResponseDto;
import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.entity.PlatformAccount;
import com.billit.loangroup_service.enums.RiskLevel;
import com.billit.loangroup_service.event.domain.LoanGroupFullEvent;
import com.billit.loangroup_service.repository.LoanGroupRepository;
import com.billit.loangroup_service.repository.PlatformAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final PlatformAccountCache platformAccountCache;
    private final PlatformAccountRepository platformAccountRepository;
    private final LoanServiceClient loanServiceClient;
    private final ApplicationEventPublisher eventPublisher;

    // 멤버 추가
    @Transactional
    public LoanGroupResponseDto assignGroup(LoanRequestClientDto request) {
        LoanResponseClientDto loanResponseClient = loanServiceClient.getLoanById(request.getLoanId());
        if (loanResponseClient == null) {
            throw new IllegalStateException("Loan response not found for loanId: " + request.getLoanId());
        }
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

        targetGroup.incrementMemberCount();
        if (targetGroup.getMemberCount() >= LoanGroup.MAX_MEMBERS) {
            targetGroup.updateGroupAsFull();
            loanGroupRepository.saveAndFlush(targetGroup);
            eventPublisher.publishEvent(new LoanGroupFullEvent(targetGroup.getGroupId()));
        }

        return LoanGroupResponseDto.from(targetGroup);
    }

    // 투자 가능한 그룹 목록 조회 -> 로직 수정 필요
    public List<LoanGroupResponseDto> getActiveGroupsWithPlatformAccount(RiskLevel riskLevel) {
        return loanGroupRepository.findAllByRiskLevelAndIsFulledTrue(riskLevel).stream()
                .filter(group -> {
                    Optional<PlatformAccount> account = platformAccountRepository.findByGroup(group);
                    return account.isPresent() && !account.get().getIsClosed();
                })
                .map(LoanGroupResponseDto::from)
                .collect(Collectors.toList());
    }

    // 특정 그룹 정보 조회
    public LoanGroupResponseDto getGroupDetails(Integer groupId) {
        LoanGroup group = loanGroupRepository.findById(Long.valueOf(groupId)).orElse(null);
        return LoanGroupResponseDto.from(group);
    }
}