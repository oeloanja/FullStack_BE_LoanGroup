package com.billit.loangroup_service.kafka.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanGroupInvestmentCompleteEvent {
    private Integer groupId;
    private BigDecimal requiredAmount;
    private BigDecimal currentBalance;
}
