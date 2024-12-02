package com.billit.loangroup_service.event.domain;


import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter
public class LoanGroupInvestmentCompleteEvent {
    private Integer groupId;
    private BigDecimal requiredAmount;
    private BigDecimal currentBalance;

    public LoanGroupInvestmentCompleteEvent(Integer groupId, BigDecimal requiredAmount, BigDecimal currentBalance) {
        this.groupId = groupId;
        this.requiredAmount = requiredAmount;
        this.currentBalance = currentBalance;
    }
}
