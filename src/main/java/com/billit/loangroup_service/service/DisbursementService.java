package com.billit.loangroup_service.service;

import com.billit.loangroup_service.connection.invest.client.InvestServiceClient;
import com.billit.loangroup_service.connection.invest.dto.RefundRequestDto;
import com.billit.loangroup_service.connection.invest.dto.SettlementRatioRequestDto;
import com.billit.loangroup_service.connection.loan.client.LoanServiceClient;
import com.billit.loangroup_service.connection.loan.dto.LoanResponseClientDto;
import com.billit.loangroup_service.connection.loan.dto.LoanStatusUpdateRequestDto;
import com.billit.loangroup_service.connection.loan.dto.LoanSuccessStatusRequestDto;
import com.billit.loangroup_service.connection.repayment.client.RepaymentClient;
import com.billit.loangroup_service.connection.repayment.dto.RepaymentRequestDto;
import com.billit.loangroup_service.connection.user.client.UserServiceClient;
import com.billit.loangroup_service.connection.user.dto.UserRequestDto;
import com.billit.loangroup_service.entity.LoanGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisbursementService {
    private final LoanServiceClient loanServiceClient;
    private final InvestServiceClient investmentServiceClient;
    private final UserServiceClient userServiceClient;
    private final RepaymentClient repaymentClient;

    @Transactional
    public void processDisbursement(LoanGroup group, BigDecimal excess) {
        List<LoanResponseClientDto> groupLoans = loanServiceClient.getLoansByGroupId(group.getGroupId());
        calculateSettlementRatio(group);
        disburseLoanAmounts(groupLoans);
        LocalDate issueDate = LocalDate.now();
        updateLoanStatuses(groupLoans, issueDate);
        investmentServiceClient.updateInvestmentDatesByGroupId(group.getGroupId());
        createRepaymentSchedules(groupLoans, issueDate);
        refundExcessInvestment(group, excess);
    }

    private void calculateSettlementRatio(LoanGroup group) {
        try {
            investmentServiceClient.updateSettlementRatioByGroupId(
                    new SettlementRatioRequestDto(group.getGroupId())
            );
        } catch (Exception e) {
            throw new RuntimeException("투자금 비율 계산 실패", e);
        }
    }

    private void disburseLoanAmounts(List<LoanResponseClientDto> groupLoans) {
        List<UserRequestDto> disbursementRequests = groupLoans.stream()
                .map(loan -> new UserRequestDto(
                        loan.getAccountBorrowId(),
                        loan.getUserBorrowId(),
                        loan.getLoanAmount(),
                        "대출금 입금"
                ))
                .collect(Collectors.toList());

        try {
            userServiceClient.requestDisbursement(disbursementRequests);
        } catch (Exception e) {
            throw new RuntimeException("대출금 입금 실패", e);
        }
    }

    private void updateLoanStatuses(List<LoanResponseClientDto> groupLoans, LocalDate issueDate) {
        List<LoanSuccessStatusRequestDto> statusUpdateRequests = groupLoans.stream()
                .map(loan -> new LoanSuccessStatusRequestDto(
                        loan.getLoanId(),
                        1,
                        issueDate
                ))
                .collect(Collectors.toList());

        try {
            log.info("Sending request data: {}", statusUpdateRequests);
            loanServiceClient.updateLoansStatusSuccess(statusUpdateRequests);
        } catch (Exception e) {
            throw new RuntimeException("대출 상태 업데이트 실패", e);
        }
    }

    private void createRepaymentSchedules(List<LoanResponseClientDto> groupLoans, LocalDate issueDate) {
        try {
            groupLoans.stream()
                    .map(request -> new RepaymentRequestDto(
                            request.getLoanId(),
                            request.getGroupId(),
                            request.getLoanAmount(),
                            request.getTerm(),
                            request.getIntRate(),
                            issueDate
                    ))
                    .forEach(repaymentClient::createRepayment);
        } catch (Exception e) {
            throw new RuntimeException("상환 생성 실패", e);
        }
    }

    private void refundExcessInvestment(LoanGroup group, BigDecimal excess) {
        if (excess.compareTo(BigDecimal.ZERO) > 0) {
            try {
                investmentServiceClient.refundUpdateInvestAmount(
                        new RefundRequestDto(group.getGroupId(), excess)
                );
            } catch (Exception e) {
                throw new RuntimeException("투자금 반환 실패", e);
            }
        }
    }
}