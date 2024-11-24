package com.billit.loangroup_service.event.domain;


import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class LoanGroupInvestmentCompleteEvent {
    private final Integer groupId;
    private final BigDecimal requiredAmount;
    private final BigDecimal currentBalance;

    public LoanGroupInvestmentCompleteEvent(Integer groupId, BigDecimal requiredAmount, BigDecimal currentBalance) {
        this.groupId = groupId;
        this.requiredAmount = requiredAmount;
        this.currentBalance = currentBalance;
    }
}
