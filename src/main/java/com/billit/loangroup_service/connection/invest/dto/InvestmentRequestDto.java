package com.billit.loangroup_service.connection.invest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter
public class InvestmentRequestDto {
    private Integer groupId;
    @NotNull(message = "투자금액은 필수입니다.")
    @Positive(message = "투자금액은 0보다 커야 합니다.")
    private BigDecimal amount;

    public InvestmentRequestDto(Integer groupId, BigDecimal amount) {
        this.groupId = groupId;
        this.amount = amount;
    }
}
