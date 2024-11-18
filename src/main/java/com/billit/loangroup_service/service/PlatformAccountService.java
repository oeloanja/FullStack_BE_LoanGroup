package com.billit.loangroup_service.service;

import com.billit.loangroup_service.cache.PlatformAccountCache;
import com.billit.loangroup_service.connection.client.LoanServiceClient;
import com.billit.loangroup_service.connection.dto.LoanResponseClientDto;
import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.entity.PlatformAccount;
import com.billit.loangroup_service.repository.LoanGroupRepository;
import com.billit.loangroup_service.repository.PlatformAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import static com.billit.loangroup_service.entity.PlatformAccount.handleAccountClosure;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlatformAccountService {
    private final PlatformAccountRepository platformAccountRepository;
    private final PlatformAccountCache platformAccountCache;
    private final LoanServiceClient loanServiceClient;
    private final LoanGroupRepository loanGroupRepository;
    private final RedisTemplate<String, PlatformAccount> redisTemplate;

    // 계좌 생성
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createPlatformAccount(LoanGroup group) {
        LoanGroup managedGroup = loanGroupRepository.findById(Long.valueOf(group.getGroupId()))
                .orElseThrow(() -> new IllegalStateException("Group not found"));

        // Loan 서비스에서 해당 그룹의 대출 목록 조회
        List<LoanResponseClientDto> groupLoans = loanServiceClient.getLoansByGroupId(group.getGroupId());

        // 총 대출금액 계산
        BigDecimal totalLoanAmount = groupLoans.stream()
                .map(LoanResponseClientDto::getLoanAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);  // add 메서드 명확히 지정

        BigDecimal averageIntRate = calculateIntRateAvg(groupLoans);
        managedGroup.updateIntRateAvg(averageIntRate);
        loanGroupRepository.save(managedGroup);  // 업데이트된 평균 이자율 저장


        PlatformAccount account = new PlatformAccount(
                managedGroup,
                totalLoanAmount,
                BigDecimal.ZERO,
                LocalDateTime.now()
        );
        platformAccountRepository.save(account);
        platformAccountCache.saveToCache(account);
    }

    // 현재 입금액 수정
    @Transactional
    public void updatePlatformAccountBalance(Integer platformAccountId, BigDecimal amount) {
        platformAccountCache.updateBalanceInCache(platformAccountId, amount);

        PlatformAccount account = platformAccountRepository.findById(platformAccountId)
                .orElseThrow(() -> new RuntimeException("Platform account not found"));
        account.updateBalance(amount);

        if (account.getIsClosed()) {
            handleAccountClosure(account);
        }
    }

    // 이자율 평균 계산
    public static BigDecimal calculateIntRateAvg(List<LoanResponseClientDto> groupLoans){
        return groupLoans.stream()
                .map(LoanResponseClientDto::getIntRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(groupLoans.size()), 2, RoundingMode.HALF_UP);
    }
}
