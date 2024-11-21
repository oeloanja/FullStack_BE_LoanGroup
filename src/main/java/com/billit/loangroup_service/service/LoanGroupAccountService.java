package com.billit.loangroup_service.service;

import com.billit.loangroup_service.cache.LoanGroupAccountCache;
import com.billit.loangroup_service.connection.invest.client.InvestServiceClient;
import com.billit.loangroup_service.connection.invest.dto.InvestmentRequestDto;
import com.billit.loangroup_service.connection.loan.client.LoanServiceClient;
import com.billit.loangroup_service.connection.loan.dto.LoanResponseClientDto;
import com.billit.loangroup_service.connection.loan.dto.LoanStatusUpdateRequestDto;
import com.billit.loangroup_service.connection.user.client.UserServiceClient;
import com.billit.loangroup_service.connection.user.dto.UserRequestDto;
import com.billit.loangroup_service.connection.user.dto.UserResponseDto;
import com.billit.loangroup_service.dto.LoanGroupAccountResponseDto;
import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.entity.LoanGroupAccount;
import com.billit.loangroup_service.exception.ClosedAccountException;
import com.billit.loangroup_service.exception.DisbursementFailedException;
import com.billit.loangroup_service.exception.LoanGroupNotFoundException;
import com.billit.loangroup_service.exception.LoanNotFoundException;
import com.billit.loangroup_service.repository.LoanGroupRepository;
import com.billit.loangroup_service.repository.LoanGroupAccountRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public void updateLoanGroupAccountBalance(Integer loanGroupId, BigDecimal amount) {
        LoanGroupAccount target = loanGroupAccountRepository.findByGroup_GroupId(loanGroupId)
                .orElseThrow(() -> new LoanGroupNotFoundException(loanGroupId));

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
        log.info("Starting disbursement for group ID: {}", group.getGroupId());
            // 1. 해당 그룹의 대출 목록 조회
            List<LoanResponseClientDto> groupLoans = loanServiceClient.getLoansByGroupId(group.getGroupId());
        log.info("Retrieved loans: {}", groupLoans);  // 대출 목록 확인
        groupLoans.forEach(loan -> {
            log.info("Loan ID: {}, AccountBorrowId: {}", loan.getLoanId(), loan.getUserBorrowAccountId());
        });

            // 2. User 서비스로 대출금 입금 요청 전송
        log.info("Creating disbursement requests...");
        List<UserRequestDto> disbursementRequests = groupLoans.stream()
                .map(loan -> {
                    log.info("Processing loan: {}", loan);
                    return new UserRequestDto(
                            loan.getUserBorrowAccountId(),
                            loan.getUserBorrowId(),
                            loan.getLoanAmount(),
                            "대출금 입금"
                    );
                })
                .collect(Collectors.toList());

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String requestBody = objectMapper.writeValueAsString(disbursementRequests);
            log.info("Disbursement Request Body: {}", requestBody);
        } catch (JsonProcessingException e) {
            log.error("Error serializing disbursement requests", e);
        }

        log.info("Disbursement requests created: {}", disbursementRequests);
            try{
                List<UserResponseDto> response = userServiceClient.requestDisbursement(disbursementRequests);
                log.info("Disbursement response: {}", response);

                BigDecimal difference = group.getLoanGroupAccount().getCurrentBalance().subtract(group.getLoanGroupAccount().getRequiredAmount());
//                if(difference.compareTo(BigDecimal.ZERO) > 0){
//                    investmentServiceClient.refundUpdateInvestAmount(
//                            new InvestmentRequestDto(group.getGroupId(), difference)
//                    );
//                }

                // 3. 대출 상태 EXECUTING으로 업데이트
                List<LoanStatusUpdateRequestDto> statusUpdateRequests = groupLoans.stream()
                        .map(loan -> new LoanStatusUpdateRequestDto(
                                loan.getLoanId(),
                                1  // EXECUTING의 status 값
                        ))
                        .collect(Collectors.toList());

                loanServiceClient.updateLoansStatus(statusUpdateRequests); // EXECUTING의 ordinal 값
//                investmentServiceClient.updateInvestmentDatesByGroupId(group.getGroupId());
            }
            catch (FeignException e){
                log.error("Feign exception during disbursement: ", e);
                throw new DisbursementFailedException(group.getGroupId());
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
