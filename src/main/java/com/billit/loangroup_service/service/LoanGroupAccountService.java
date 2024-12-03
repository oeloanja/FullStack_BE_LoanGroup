package com.billit.loangroup_service.service;

import com.billit.loangroup_service.connection.loan.client.LoanServiceClient;
import com.billit.loangroup_service.connection.loan.dto.LoanResponseClientDto;
import com.billit.loangroup_service.dto.LoanGroupAccountRequestDto;
import com.billit.loangroup_service.dto.LoanGroupAccountResponseDto;
import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.entity.LoanGroupAccount;
import com.billit.loangroup_service.exception.LoanGroupNotFoundException;
import com.billit.loangroup_service.kafka.event.domain.LoanGroupInvestmentCompleteEvent;
import com.billit.loangroup_service.repository.LoanGroupRepository;
import com.billit.loangroup_service.repository.LoanGroupAccountRepository;
import com.billit.loangroup_service.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LoanGroupAccountService {
    private final LoanGroupAccountRepository loanGroupAccountRepository;
    private final LoanServiceClient loanServiceClient;
    private final LoanGroupRepository loanGroupRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final DisbursementService disbursementService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createLoanGroupAccount(LoanGroup group) {
        Optional<LoanGroup> optionalGroup = loanGroupRepository.findById(Long.valueOf(group.getGroupId()));
        LoanGroup managedGroup = optionalGroup.orElseThrow(() -> new LoanGroupNotFoundException(group.getGroupId()));

        List<LoanResponseClientDto> groupLoans = loanServiceClient.getLoansByGroupId(group.getGroupId());
        if (groupLoans.isEmpty()) {
            return;
        }

        BigDecimal totalLoanAmount = groupLoans.stream()
                .map(LoanResponseClientDto::getLoanAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageIntRate = calculateIntRateAvg(groupLoans);

        managedGroup.updateIntRateAvg(averageIntRate);
        loanGroupRepository.save(managedGroup);
        loanGroupRepository.flush();

        LoanGroupAccount account = new LoanGroupAccount(
                managedGroup,
                totalLoanAmount,
                BigDecimal.ZERO,
                LocalDateTime.now()
        );
        loanGroupAccountRepository.save(account);
    }

    @Transactional
    public void updateLoanGroupAccountBalance(LoanGroupAccountRequestDto investRequest) {
        LoanGroupAccount target = loanGroupAccountRepository.findByGroup_GroupId(investRequest.getGroupId())
                .orElseThrow(() -> new LoanGroupNotFoundException(investRequest.getGroupId()));

        ValidationUtils.validateAccountNotClosed(target);

        BigDecimal newBalance = target.getCurrentBalance().add(investRequest.getAmount());
        target.updateBalance(investRequest.getAmount());

        if (newBalance.compareTo(target.getRequiredAmount()) >= 0) {
            target.closeAccount();
            loanGroupAccountRepository.saveAndFlush(target);

            kafkaTemplate.send("investment-complete",
                    new LoanGroupInvestmentCompleteEvent(
                            target.getGroup().getGroupId(),
                            target.getRequiredAmount(),
                            newBalance
                    ));
        }
    }

    public BigDecimal calculateIntRateAvg(List<LoanResponseClientDto> groupLoans) {
        return groupLoans.stream()
                .map(LoanResponseClientDto::getIntRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(groupLoans.size()), 2, RoundingMode.HALF_UP);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processDisbursement(LoanGroup group, BigDecimal excess) {
        disbursementService.processDisbursement(group, excess);
    }

    public LoanGroupAccountResponseDto getAccount(Integer groupId) {
        LoanGroupAccount target = loanGroupAccountRepository.findByGroup_GroupId(groupId)
                .orElseThrow(() -> new LoanGroupNotFoundException(groupId));
        return LoanGroupAccountResponseDto.from(target);
    }
}