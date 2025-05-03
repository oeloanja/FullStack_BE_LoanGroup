package com.billit.loangroup_service.kafka.consumer;

import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.kafka.event.LoanGroupFullEvent;
import com.billit.loangroup_service.kafka.event.LoanGroupInvestmentCompleteEvent;
import com.billit.loangroup_service.repository.LoanGroupRepository;
import com.billit.loangroup_service.service.LoanGroupAccountService;
import com.billit.loangroup_service.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.kafka.annotation.KafkaListener;
import java.math.BigDecimal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanGroupEventConsumer {
    private final LoanGroupAccountService loanGroupAccountService;
    private final LoanGroupRepository loanGroupRepository;

    @KafkaListener(
            topics = "loan-group-full",
            groupId = "loan-group-service",
            containerFactory = "loanGroupFullEventKafkaListenerContainerFactory"
    )
    public void handleLoanGroupFullEvent(LoanGroupFullEvent event) {
            Optional<LoanGroup> group = loanGroupRepository.findById(Long.valueOf(event.getGroupId()));
            ValidationUtils.validateLoanGroupExistence(group, event.getGroupId());
            loanGroupAccountService.createLoanGroupAccount(group.get());
            log.info("Successfully processed loan group full event for groupId: {}", event.getGroupId());
    }

    @KafkaListener(
            topics = "investment-complete",
            groupId = "loan-group-service",
            containerFactory = "loanGroupInvestmentCompleteEventKafkaListenerContainerFactory"
    )
    public void handleInvestmentCompleteEvent(LoanGroupInvestmentCompleteEvent event) {
        log.info("Received investment complete event for groupId: {}", event.getGroupId());
            Optional<LoanGroup> group = loanGroupRepository.findById(Long.valueOf(event.getGroupId()));
            ValidationUtils.validateLoanGroupExistence(group, event.getGroupId());

            BigDecimal excess = event.getCurrentBalance().subtract(event.getRequiredAmount());
            loanGroupAccountService.processDisbursement(group.get(), excess);

            log.info("Successfully processed investment complete event for groupId: {}", event.getGroupId());
    }
}
