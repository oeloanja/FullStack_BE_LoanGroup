package com.billit.loangroup_service.cache;

import com.billit.loangroup_service.entity.LoanGroupAccount;
import com.billit.loangroup_service.repository.LoanGroupAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class LoanGroupAccountCache {
    private static final String ACCOUNT_KEY_PREFIX = "platform_account:";
    private static final long CACHE_DURATION = 24 * 60 * 60;

    private final RedisTemplate<String, LoanGroupAccount> redisTemplate;
    private final LoanGroupAccountRepository loanGroupAccountRepository;

    public void saveToCache(LoanGroupAccount account) {
        String key = generateKey(account.getPlatformAccountId());
        redisTemplate.opsForValue().set(key, account, Duration.ofSeconds(CACHE_DURATION));
    }

    public LoanGroupAccount getFromCache(Integer accountId) {
        String key = generateKey(accountId);
        LoanGroupAccount account = redisTemplate.opsForValue().get(key);

        if (account == null) {
            account = loanGroupAccountRepository.findById(accountId)
                    .orElseThrow(() -> new RuntimeException("Platform account not found"));
            saveToCache(account);
        }

        return account;
    }

    public void updateBalanceInCache(Integer accountId, BigDecimal amount) {
        String key = generateKey(accountId);
        redisTemplate.execute(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                boolean isMultiStarted = false;
                try {
                    operations.watch(key);

                    LoanGroupAccount account = getFromCache(accountId);
                    if (account.getIsClosed()) {
                        throw new IllegalStateException("Account is already closed");
                    }

                    operations.multi();
                    isMultiStarted = true;

                    account.updateBalance(amount);
                    saveToCache(account);

                    return operations.exec();
                } catch (Exception e) {
                    if (isMultiStarted) {
                        operations.discard();
                    }
                    throw e;
                }
            }
        });
    }

    private String generateKey(Integer accountId) {
        return ACCOUNT_KEY_PREFIX + accountId;
    }
}
