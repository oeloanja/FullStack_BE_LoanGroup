package com.billit.loangroup_service.service;

import com.billit.loangroup_service.cache.LoanGroupAccountCache;
import com.billit.loangroup_service.connection.invest.client.InvestServiceClient;
import com.billit.loangroup_service.connection.loan.client.LoanServiceClient;
import com.billit.loangroup_service.connection.loan.dto.LoanResponseClientDto;
import com.billit.loangroup_service.connection.loan.dto.LoanStatusUpdateRequestDto;
import com.billit.loangroup_service.connection.user.client.UserServiceClient;
import com.billit.loangroup_service.dto.LoanGroupAccountResponseDto;
import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.entity.LoanGroupAccount;
import com.billit.loangroup_service.exception.ClosedAccountException;
import com.billit.loangroup_service.exception.DisbursementFailedException;
import com.billit.loangroup_service.exception.LoanGroupNotFoundException;
import com.billit.loangroup_service.exception.LoanNotFoundException;
import com.billit.loangroup_service.repository.LoanGroupRepository;
import com.billit.loangroup_service.repository.LoanGroupAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private final RedisTemplate<String, LoanGroupAccount> redisTemplate;

    // 계좌 생성
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createLoanGroupAccount(LoanGroup group) {
        LoanGroup managedGroup = loanGroupRepository.findById(Long.valueOf(group.getGroupId()))
                .orElseThrow(() -> new LoanGroupNotFoundException(group.getGroupId()));


        // Loan 서비스에서 해당 그룹의 대출 목록 조회
        List<LoanResponseClientDto> groupLoans = loanServiceClient.getLoansByGroupId(group.getGroupId());
        if (groupLoans == null || groupLoans.isEmpty()) {
            throw new LoanNotFoundException(group.getGroupId());
        }

        // 총 대출금액 계산
        BigDecimal totalLoanAmount = groupLoans.stream()
                .map(LoanResponseClientDto::getLoanAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);  // add 메서드 명확히 지정

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
    public void updateLoanGroupAccountBalance(Integer loanGroupId, BigDecimal amount) {
        LoanGroupAccount target = loanGroupAccountRepository.findByGroup_GroupId(loanGroupId);
        if (target == null) {
            throw new LoanGroupNotFoundException(loanGroupId);
        }

        if (target.getIsClosed()) {
            throw new ClosedAccountException(target.getLoanGroupAccountId());
        }

        target.updateBalance(amount);
        loanGroupAccountCache.updateBalanceInCache(target.getLoanGroupAccountId(), amount);

        if (target.getCurrentBalance().compareTo(target.getRequiredAmount()) >= 0) {
            target.closeAccount();
            processDisbursement(target.getGroup());
        }
    }

    private void processDisbursement(LoanGroup group) {
            // 1. 해당 그룹의 대출 목록 조회
            List<LoanResponseClientDto> groupLoans = loanServiceClient.getLoansByGroupId(group.getGroupId());
        if (groupLoans == null || groupLoans.isEmpty()) {
            throw new LoanNotFoundException(group.getGroupId());
        }

//            // 2. User 서비스로 대출금 입금 요청 전송
//            List<UserRequestDto> disbursementRequests = groupLoans.stream()
//                    .map(loan -> new UserRequestDto(
//                            loan.getAccountBorrowId(),
//                            loan.getLoanAmount()
//                    ))
//                    .collect(Collectors.toList());
//
//            boolean isSuccess = userServiceClient.requestDisbursement(disbursementRequests);
            if(true){
            //if (isSuccess) {

                // Investment 서비스에 investmentDate update 요청
//                investmentServiceClient.updateInvestmentDatesByGroupId(group.getGroupId());

                // 3. 대출 상태 EXECUTING으로 업데이트
                List<LoanStatusUpdateRequestDto> statusUpdateRequests = groupLoans.stream()
                        .map(loan -> new LoanStatusUpdateRequestDto(
                                loan.getLoanId(),
                                1  // EXECUTING의 status 값
                        ))
                        .collect(Collectors.toList());

                loanServiceClient.updateLoansStatus(statusUpdateRequests); // EXECUTING의 ordinal 값
            } else {
                throw new DisbursementFailedException(group.getGroupId());
            }
        }

    // GroupId로 Account 찾기
    public LoanGroupAccountResponseDto getAccount(Integer groupId) {
        LoanGroupAccount account = loanGroupAccountRepository.findByGroup_GroupId(groupId);
        return LoanGroupAccountResponseDto.from(account);
    }

    // 이자율 평균 계산
    public BigDecimal calculateIntRateAvg(List<LoanResponseClientDto> groupLoans){
        return groupLoans.stream()
                .map(LoanResponseClientDto::getIntRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(groupLoans.size()), 2, RoundingMode.HALF_UP);
    }
}
