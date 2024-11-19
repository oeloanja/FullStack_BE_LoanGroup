package com.billit.loangroup_service.connection.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class UserRequestDto {
    private Integer accountBorrowId;
    private BigDecimal loanAmount;
}
