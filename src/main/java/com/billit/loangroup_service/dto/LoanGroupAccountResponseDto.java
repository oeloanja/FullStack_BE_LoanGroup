package com.billit.loangroup_service.dto;

import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.entity.LoanGroupAccount;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class LoanGroupAccountResponseDto {
    private final Integer platformAccountId;
    private final LoanGroup group;
    private final BigDecimal requiredAmount;
    private final BigDecimal currentBalance;
    private final Boolean isClosed;
    private final LocalDateTime createdAt;

    public LoanGroupAccountResponseDto(LoanGroupAccount loanGroupAccount) {
        this.platformAccountId = loanGroupAccount.getPlatformAccountId();
        this.group = loanGroupAccount.getGroup();
        this.requiredAmount = loanGroupAccount.getRequiredAmount();
        this.currentBalance = loanGroupAccount.getCurrentBalance();
        this.isClosed = loanGroupAccount.getIsClosed();
        this.createdAt = loanGroupAccount.getCreatedAt();
    }

    public static LoanGroupAccountResponseDto from(LoanGroupAccount loanGroupAccount) {
        return new LoanGroupAccountResponseDto(loanGroupAccount);
    }
}
