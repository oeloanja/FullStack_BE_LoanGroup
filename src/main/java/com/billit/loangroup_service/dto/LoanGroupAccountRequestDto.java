package com.billit.loangroup_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoanGroupAccountRequestDto {
    private Integer groupId;
    private BigDecimal amount;
}
