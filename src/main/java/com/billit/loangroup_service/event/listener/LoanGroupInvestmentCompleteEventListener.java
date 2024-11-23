package com.billit.loangroup_service.event.listener;

import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.event.domain.LoanGroupInvestmentCompleteEvent;
import com.billit.loangroup_service.repository.LoanGroupRepository;
import com.billit.loangroup_service.service.LoanGroupAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class LoanGroupInvestmentCompleteEventListener {
    private final LoanGroupAccountService loanGroupAccountService;
    private final LoanGroupRepository loanGroupRepository;

    @EventListener
    @Async
    public void handleLoanGroupInvestmentComplete(LoanGroupInvestmentCompleteEvent event) {
        processInvestmentCompleteWithNewTransaction(
                event.getGroupId(),
                event.getRequiredAmount(),
                event.getCurrentBalance()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processInvestmentCompleteWithNewTransaction(
            Integer groupId,
            BigDecimal requiredAmount,
            BigDecimal currentBalance
    ) {
        LoanGroup group = loanGroupRepository.findById(Long.valueOf(groupId))
                .orElseThrow(() -> new IllegalStateException("Group not found"));

        BigDecimal excess = currentBalance.subtract(requiredAmount);
        loanGroupAccountService.processDisbursement(group, excess);
    }
}
