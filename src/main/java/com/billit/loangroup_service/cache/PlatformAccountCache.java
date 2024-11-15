package com.billit.loangroup_service.cache;

import com.billit.loangroup_service.entity.PlatformAccount;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class PlatformAccountCache {
    private RedisTemplate<String, PlatformAccount> redisTemplate;

    public void saveToCache(String key, PlatformAccount account) {
        redisTemplate.opsForValue().set(key, account);
    }

    public PlatformAccount getFromCache(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
