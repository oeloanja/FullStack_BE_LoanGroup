package com.billit.loangroup_service.service;

import com.billit.loangroup_service.cache.LoanGroupAccountCache;
import com.billit.loangroup_service.connection.invest.client.InvestServiceClient;
import com.billit.loangroup_service.connection.invest.dto.RefundRequestDto;
import com.billit.loangroup_service.connection.invest.dto.SettlementRatioRequestDto;
import com.billit.loangroup_service.connection.loan.client.LoanServiceClient;
import com.billit.loangroup_service.connection.loan.dto.LoanResponseClientDto;
import com.billit.loangroup_service.connection.loan.dto.LoanStatusUpdateRequestDto;
import com.billit.loangroup_service.connection.repayment.client.RepaymentClient;
import com.billit.loangroup_service.connection.repayment.dto.RepaymentRequestDto;
import com.billit.loangroup_service.connection.user.client.UserServiceClient;
import com.billit.loangroup_service.connection.user.dto.UserRequestDto;
import com.billit.loangroup_service.connection.user.dto.UserResponseDto;
import com.billit.loangroup_service.dto.LoanGroupAccountRequestDto;
import com.billit.loangroup_service.dto.LoanGroupAccountResponseDto;
import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.entity.LoanGroupAccount;
import com.billit.loangroup_service.event.domain.LoanGroupInvestmentCompleteEvent;
import com.billit.loangroup_service.exception.ClosedAccountException;
import com.billit.loangroup_service.exception.LoanGroupNotFoundException;
import com.billit.loangroup_service.exception.LoanNotFoundException;
import com.billit.loangroup_service.repository.LoanGroupRepository;
import com.billit.loangroup_service.repository.LoanGroupAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LoanGroupAccountService {
    private final LoanGroupAccountRepository loanGroupAccountRepository;
    private final LoanGroupAccountCache loanGroupAccountCache;
    private final LoanServiceClient loanServiceClient;
    private final UserServiceClient userServiceClient;
    private final LoanGroupRepository loanGroupRepository;
    private final InvestServiceClient investmentServiceClient;
    private final RepaymentClient repaymentClient;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisTemplate<String, LoanGroupAccount> redisTemplate;

    // 계좌 생성
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createLoanGroupAccount(LoanGroup group) {
        LoanGroup managedGroup = loanGroupRepository.findById(Long.valueOf(group.getGroupId()))
                .orElseThrow(() -> new LoanGroupNotFoundException(group.getGroupId()));


        // Loan 서비스에서 해당 그룹의 대출 목록 조회
        List<LoanResponseClientDto> groupLoans = loanServiceClient.getLoansByGroupId(group.getGroupId());
        if (groupLoans.isEmpty()) {
            throw new LoanNotFoundException(group.getGroupId());
        }

        // 총 대출금액 계산
        BigDecimal totalLoanAmount = groupLoans.stream()
                .map(LoanResponseClientDto::getLoanAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageIntRate = calculateIntRateAvg(groupLoans);
        managedGroup.updateIntRateAvg(averageIntRate);
        loanGroupRepository.save(managedGroup);  // 업데이트된 평균 이자율 저장


        LoanGroupAccount account = new LoanGroupAccount(
                managedGroup,
                totalLoanAmount,
                BigDecimal.ZERO,
                LocalDateTime.now()
        );
        loanGroupAccountRepository.save(account);
        loanGroupAccountCache.saveToCache(account);
    }

    // 현재 입금액 수정: LoanGroupAccount entity 데이터가 편집됨
    @Transactional
    public void updateLoanGroupAccountBalance(LoanGroupAccountRequestDto investRequest) {
        LoanGroupAccount target = loanGroupAccountRepository.findByGroup_GroupId(investRequest.getGroupId())
                .orElseThrow(() -> new LoanGroupNotFoundException(investRequest.getGroupId()));

        if (target.getIsClosed()) {
            throw new ClosedAccountException(target.getLoanGroupAccountId());
        }

        BigDecimal newBalance = target.getCurrentBalance().add(investRequest.getAmount());

        // 일단 잔액 업데이트
        target.updateBalance(investRequest.getAmount());
        loanGroupAccountCache.updateBalanceInCache(target.getLoanGroupAccountId(), investRequest.getAmount());

        // 목표금액 도달/초과 시 이벤트 발행
        if (newBalance.compareTo(target.getRequiredAmount()) >= 0) {
            log.info("Investment target amount reached or exceeded for group: {}. Publishing event...",
                    target.getGroup().getGroupId());

            target.closeAccount(); // 계좌 상태를 먼저 closed로 변경
            loanGroupAccountRepository.save(target);

            eventPublisher.publishEvent(new LoanGroupInvestmentCompleteEvent(
                    target.getGroup().getGroupId(),
                    target.getRequiredAmount(),
                    newBalance
            ));
        }
    }

    public void processDisbursement(LoanGroup group, BigDecimal excess) {
        List<LoanResponseClientDto> groupLoans = loanServiceClient.getLoansByGroupId(group.getGroupId());

        // 1. 투자 정산 비율 계산 (이제 마지막 투자까지 포함됨)
        try {
            investmentServiceClient.updateSettlementRatioByGroupId(
                    new SettlementRatioRequestDto(group.getGroupId())
            );
        } catch (Exception e) {
            throw new RuntimeException("투자금 비율 계산 실패");
        }

        // 2. 대출금 입금 처리
        List<UserRequestDto> disbursementRequests = groupLoans.stream()
                .map(loan -> {
                    UserRequestDto request = new UserRequestDto(
                            loan.getAccountBorrowId(),
                            loan.getUserBorrowId(),
                            loan.getLoanAmount(),
                            "대출금 입금"
                    );
                    log.info("Created disbursement request: {}", request);
                    return request;
                })
                .collect(Collectors.toList());

        try {
            userServiceClient.requestDisbursement(disbursementRequests);
        } catch (Exception e) {
            throw new RuntimeException("대출금 입금 실패");
        }

        // 3. 대출 상태 업데이트
        List<LoanStatusUpdateRequestDto> statusUpdateRequests = groupLoans.stream()
                .map(loan -> new LoanStatusUpdateRequestDto(
                        loan.getLoanId(),
                        1  // EXECUTING의 status 값
                ))
                .collect(Collectors.toList());

        try {
            loanServiceClient.updateLoansStatus(statusUpdateRequests);
        } catch (Exception e) {
            throw new RuntimeException("대출 상태 업데이트 실패");
        }

        // 4. 투자 실행일자 업데이트
        try {
            investmentServiceClient.updateInvestmentDatesByGroupId(group.getGroupId());
        } catch (Exception e) {
            throw new RuntimeException("투자 실행일자 작성 실패");
        }

        // 5. 상환 스케줄 생성
        try {
            groupLoans.stream()
                    .map(request -> new RepaymentRequestDto(
                            request.getLoanId(),
                            request.getGroupId(),
                            request.getLoanAmount(),
                            request.getTerm(),
                            request.getIntRate(),
                            request.getIssueDate()
                    ))
                    .forEach(repaymentClient::createRepayment);
        } catch (Exception e) {
            throw new RuntimeException("상환 생성 실패");
        }

        // 6. 초과 투자금 반환 (정확한 비율로 계산된 상태에서 실행)
        if (excess.compareTo(BigDecimal.ZERO) > 0) {
            try {
                investmentServiceClient.refundUpdateInvestAmount(
                        new RefundRequestDto(group.getGroupId(), excess)
                );
            } catch (Exception e) {
                throw new RuntimeException("투자금 반환 실패");
            }
        }
    }

    // GroupId로 Account 찾기
    public LoanGroupAccountResponseDto getAccount(Integer groupId) {
        return loanGroupAccountRepository.findByGroup_GroupId(groupId)
                .map(LoanGroupAccountResponseDto::from)
                .orElseThrow(() -> new LoanGroupNotFoundException(groupId));
    }

    // 이자율 평균 계산
    public BigDecimal calculateIntRateAvg(List<LoanResponseClientDto> groupLoans){
        return groupLoans.stream()
                .map(LoanResponseClientDto::getIntRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(groupLoans.size()), 2, RoundingMode.HALF_UP);
    }
}
