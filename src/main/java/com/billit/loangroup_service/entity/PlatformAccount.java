package com.billit.loangroup_service.entity;

import jakarta.persistence.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;

@RedisHash("PlatformAccount")
public class PlatformAccount implements Serializable {
    @Id
    private Long platformAccountId;

    private Long groupId;
    private Long requiredAmount;
    private Long currentBalance;
    private Boolean isClosed = false;
    private LocalDateTime createdAt;

}
