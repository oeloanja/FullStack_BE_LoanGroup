package com.billit.loangroup_service.dto;

import com.billit.loangroup_service.entity.LoanGroupAccount;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class LoanGroupAccountResponseDto {
    private final Integer loanGroupAccountId;
    private final BigDecimal requiredAmount;
    private final BigDecimal currentBalance;
    private final Boolean isClosed;
    private final LocalDateTime createdAt;

    public LoanGroupAccountResponseDto(LoanGroupAccount loanGroupAccount) {
        this.loanGroupAccountId = loanGroupAccount.getLoanGroupAccountId();
        this.requiredAmount = loanGroupAccount.getRequiredAmount();
        this.currentBalance = loanGroupAccount.getCurrentBalance();
        this.isClosed = loanGroupAccount.getIsClosed();
        this.createdAt = loanGroupAccount.getCreatedAt();
    }

    public static LoanGroupAccountResponseDto from(LoanGroupAccount loanGroupAccount) {
        return new LoanGroupAccountResponseDto(loanGroupAccount);
    }
}
