package com.billit.loangroup_service.kafka.compensation.handler;

import com.billit.loangroup_service.entity.LoanGroupAccount;
import com.billit.loangroup_service.kafka.compensation.event.*;
import com.billit.loangroup_service.service.LoanGroupAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompensationHandler {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final LoanGroupAccountService loanGroupAccountService;

    @KafkaListener(topics = "disbursement-compensation")
    public void handleCompensation(DisbursementCompensationEvent event) {
        String key = String.valueOf(event.getGroupId());

        try {
            // 실패 단계에 따른 보상 처리
            switch (event.getFailedStep()) {
                case EXCESS_REFUND:
                    break;

                case REPAYMENT_SCHEDULE:
                    kafkaTemplate.send("repayment-schedule-compensation", key,
                            new RepaymentScheduleCompensationEvent(event.getGroupId()));
                    break;

                case INVESTMENT_DATE_UPDATE:
                    kafkaTemplate.send("repayment-schedule-compensation", key,
                            new RepaymentScheduleCompensationEvent(event.getGroupId()));
                    kafkaTemplate.send("investment-date-compensation", key,
                            new InvestmentDateCompensationEvent(event.getGroupId()));
                    break;

                case STATUS_UPDATE:
                    kafkaTemplate.send("repayment-schedule-compensation", key,
                            new RepaymentScheduleCompensationEvent(event.getGroupId()));
                    kafkaTemplate.send("investment-date-compensation", key,
                            new InvestmentDateCompensationEvent(event.getGroupId()));
                    event.getGroupLoans().forEach(loan ->
                            kafkaTemplate.send("loan-status-compensation", key,
                                    new LoanStatusCompensationEvent(loan.getLoanId()))
                    );
                    break;

                case LOAN_DISBURSEMENT:
                    kafkaTemplate.send("repayment-schedule-compensation", key,
                            new RepaymentScheduleCompensationEvent(event.getGroupId()));
                    kafkaTemplate.send("investment-date-compensation", key,
                            new InvestmentDateCompensationEvent(event.getGroupId()));
                    event.getGroupLoans().forEach(loan ->
                            kafkaTemplate.send("loan-status-compensation", key,
                                    new LoanStatusCompensationEvent(loan.getLoanId()))
                    );
                    // 대출금 입금 실패는 자동으로 롤백됨
                    break;

                case SETTLEMENT:
                    kafkaTemplate.send("settlement-compensation", key,
                            new SettlementCompensationEvent(event.getGroupId()));
                    break;
            }

            // 마지막 투자금 환급이 필요한 경우
            if (event.getLastInvestAmount().compareTo(BigDecimal.ZERO) > 0) {
                kafkaTemplate.send("last-investment-refund", key,
                        new LastInvestmentRefundEvent(
                                event.getGroupId(),
                                event.getLastInvestAmount()
                        ));
            }

            // 대출그룹 계좌 상태 업데이트
            loanGroupAccountService.updateAccountStatusOpened(event.getGroupId());

        } catch (Exception e) {
            log.error("Compensation failed for groupId: {}, step: {}",
                    event.getGroupId(), event.getFailedStep(), e);
        }
    }
}

