package com.billit.loangroup_service.connection.invest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequestDto {
    private Integer groupId;
    private BigDecimal remainingAmount;
}
