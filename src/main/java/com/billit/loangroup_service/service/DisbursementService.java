package com.billit.loangroup_service.service;

import com.billit.loangroup_service.connection.loan.client.LoanServiceClient;
import com.billit.loangroup_service.connection.loan.dto.LoanResponseClientDto;
import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.kafka.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisbursementService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final LoanServiceClient loanServiceClient;

    @Transactional
    public void processDisbursement(LoanGroup group, BigDecimal excess) {
        List<LoanResponseClientDto> groupLoans = loanServiceClient.getLoansByGroupId(group.getGroupId());
        LocalDate issueDate = LocalDate.now();

        List<LoanResponseClientEventDto> eventDto = convertToEventDto(groupLoans);


        // 각 단계별 이벤트 발행
        sendCalculationEvent(group.getGroupId());
        sendDisbursementEvent(eventDto, group.getGroupId());
        sendStatusUpdateEvent(eventDto, group.getGroupId(), issueDate);
        sendInvestmentDateUpdateEvent(group.getGroupId());
        sendRepaymentScheduleEvent(groupLoans, group.getGroupId(), issueDate);

        if (excess.compareTo(BigDecimal.ZERO) > 0) {
            sendExcessRefundEvent(group.getGroupId(), excess);
        }
    }

    private void sendCalculationEvent(Integer groupId) {
        String key = String.valueOf(groupId);
        SettlementCalculationEvent event = new SettlementCalculationEvent(groupId, "INITIATED");

        kafkaTemplate.send("settlement-calculation", key, event)
                .whenComplete((success, failure) -> {
                    if (failure == null) {
                        log.info("Settlement calculation event sent, partition: {}",
                                success.getRecordMetadata().partition());
                    } else {
                        log.error("Settlement calculation event failed", failure);
                    }
                });
    }

    private void sendDisbursementEvent(List<LoanResponseClientEventDto> groupLoans, Integer groupId) {
        String key = String.valueOf(groupId);
        LoanDisbursementEvent event = new LoanDisbursementEvent(groupLoans, groupId, "INITIATED");

        kafkaTemplate.send("loan-disbursement", key, event)
                .whenComplete((success, failure) -> {
                    if (failure == null) {
                        log.info("Loan disbursement event sent, partition: {}",
                                success.getRecordMetadata().partition());
                    } else {
                        log.error("Loan disbursement event failed", failure);
                    }
                });
    }

    private void sendStatusUpdateEvent(List<LoanResponseClientEventDto> groupLoans, Integer groupId, LocalDate issueDate) {
        String key = String.valueOf(groupId);
        LoanStatusUpdateEvent event = new LoanStatusUpdateEvent(groupLoans, groupId, issueDate, "INITIATED");

        kafkaTemplate.send("loan-status-update", key, event)
                .whenComplete((success, failure) -> {
                    if (failure == null) {
                        log.info("Loan status update event sent, partition: {}",
                                success.getRecordMetadata().partition());
                    } else {
                        log.error("Loan status update event failed", failure);
                    }
                });
    }

    private void sendInvestmentDateUpdateEvent(Integer groupId) {
        String key = String.valueOf(groupId);
        kafkaTemplate.send("investment-date-update", key, groupId)
                .whenComplete((success, failure) -> {
                    if (failure == null) {
                        log.info("Investment date update event sent, partition: {}",
                                success.getRecordMetadata().partition());
                    } else {
                        log.error("Investment date update event failed", failure);
                    }
                });
    }

    private void sendRepaymentScheduleEvent(List<LoanResponseClientDto> groupLoans, Integer groupId, LocalDate issueDate) {
        String key = String.valueOf(groupId);
        List<LoanResponseClientEventDto> eventDto = convertToEventDto(groupLoans);
        RepaymentScheduleEvent event = new RepaymentScheduleEvent(eventDto, groupId, issueDate, "INITIATED");

        kafkaTemplate.send("repayment-schedule", key, event)
                .whenComplete((success, failure) -> {
                    if (failure == null) {
                        log.info("Repayment schedule event sent, partition: {}",
                                success.getRecordMetadata().partition());
                    } else {
                        log.error("Repayment schedule event failed", failure);
                    }
                });
    }

    private void sendExcessRefundEvent(Integer groupId, BigDecimal excess) {
        String key = String.valueOf(groupId);
        ExcessRefundEvent event = new ExcessRefundEvent(groupId, excess, "INITIATED");

        kafkaTemplate.send("excess-refund", key, event)
                .whenComplete((success, failure) -> {
                    if (failure == null) {
                        log.info("Excess refund event sent, partition: {}",
                                success.getRecordMetadata().partition());
                    } else {
                        log.error("Excess refund event failed", failure);
                    }
                });
    }
    private List<LoanResponseClientEventDto> convertToEventDto(List<LoanResponseClientDto> groupLoans) {
        return groupLoans.stream()
                .map(dto -> new LoanResponseClientEventDto(
                        dto.getLoanId(),
                        dto.getGroupId(),
                        dto.getAccountBorrowId(),
                        dto.getUserBorrowId(),
                        dto.getLoanAmount(),
                        dto.getTerm(),
                        dto.getIntRate()
                ))
                .toList();
    }

}