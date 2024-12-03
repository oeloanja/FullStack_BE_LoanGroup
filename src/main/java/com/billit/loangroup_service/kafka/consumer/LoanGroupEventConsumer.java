package com.billit.loangroup_service.kafka.consumer;

import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.kafka.event.domain.LoanGroupFullEvent;
import com.billit.loangroup_service.kafka.event.domain.LoanGroupInvestmentCompleteEvent;
import com.billit.loangroup_service.exception.LoanGroupNotFoundException;
import com.billit.loangroup_service.repository.LoanGroupRepository;
import com.billit.loangroup_service.service.LoanGroupAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.kafka.annotation.KafkaListener;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanGroupEventConsumer {
    private final LoanGroupAccountService loanGroupAccountService;
    private final LoanGroupRepository loanGroupRepository;

    @KafkaListener(topics = "loan-group-full", groupId = "loan-group-service")
    public void handleLoanGroupFullEvent(LoanGroupFullEvent event) {
        try {
            LoanGroup group = loanGroupRepository.findById(Long.valueOf(event.getGroupId()))
                    .orElseThrow(() -> new LoanGroupNotFoundException(event.getGroupId()));
            loanGroupAccountService.createLoanGroupAccount(group);
        } catch (Exception e) {
            log.error("Error processing loan group full event", e);
        }
    }

    @KafkaListener(topics = "investment-complete", groupId = "loan-group-service")
    public void handleInvestmentCompleteEvent(LoanGroupInvestmentCompleteEvent event) {
        try {
            LoanGroup group = loanGroupRepository.findById(Long.valueOf(event.getGroupId()))
                    .orElseThrow(() -> new LoanGroupNotFoundException(event.getGroupId()));
            BigDecimal excess = event.getCurrentBalance().subtract(event.getRequiredAmount());
            loanGroupAccountService.processDisbursement(group, excess);
        } catch (Exception e) {
            log.error("Error processing investment complete event", e);
        }
    }
}
