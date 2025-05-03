package com.billit.loangroup_service.kafka.compensation.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LastInvestmentRefundEvent {
    private Integer groupId;
    private BigDecimal amount;
}
