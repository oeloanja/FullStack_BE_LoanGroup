package com.billit.loangroup_service.connection.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter
public class InvestmentRequestDto {
    private Integer groupId;
    private BigDecimal amount;

    public InvestmentRequestDto(Integer groupId, BigDecimal amount) {
        this.groupId = groupId;
        this.amount = amount;
    }
}
