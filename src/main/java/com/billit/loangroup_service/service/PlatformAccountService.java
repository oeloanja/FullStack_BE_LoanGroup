package com.billit.loangroup_service.service;

import com.billit.loangroup_service.cache.PlatformAccountCache;
import com.billit.loangroup_service.entity.PlatformAccount;
import com.billit.loangroup_service.repository.LoanGroupRepository;
import com.billit.loangroup_service.repository.PlatformAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.billit.loangroup_service.entity.PlatformAccount.handleAccountClosure;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlatformAccountService {
    private final PlatformAccountRepository platformAccountRepository;
    private final PlatformAccountCache platformAccountCache;
    private final LoanGroupRepository loanGroupRepository;
    private final RedisTemplate<String, PlatformAccount> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "platform_account:";

    // 투자금 입금
    @Transactional
    public void processInvestment(Integer platformAccountId, BigDecimal investmentAmount) {
        // 캐시에서 계좌 정보 업데이트
        platformAccountCache.updateBalanceInCache(platformAccountId, investmentAmount);

        // DB에서 계좌 정보 업데이트
        PlatformAccount account = platformAccountRepository.findById(platformAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Platform account not found"));

        account.updateBalance(investmentAmount);

        if (account.getIsClosed()) {
            handleAccountClosure(account);
        }
    }

}
