package com.billit.loangroup_service.dto;

import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.entity.PlatformAccount;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class PlatformAccountResponseDto {
    private final Integer platformAccountId;
    private final LoanGroup group;
    private final BigDecimal requiredAmount;
    private final BigDecimal currentBalance;
    private final Boolean isClosed;
    private final LocalDateTime createdAt;

    public PlatformAccountResponseDto(PlatformAccount platformAccount) {
        this.platformAccountId = platformAccount.getPlatformAccountId();
        this.group = platformAccount.getGroup();
        this.requiredAmount = platformAccount.getRequiredAmount();
        this.currentBalance = platformAccount.getCurrentBalance();
        this.isClosed = platformAccount.getIsClosed();
        this.createdAt = platformAccount.getCreatedAt();
    }

    public static PlatformAccountResponseDto from(PlatformAccount platformAccount) {
        return new PlatformAccountResponseDto(platformAccount);
    }
}
