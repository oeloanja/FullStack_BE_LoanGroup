package com.billit.loangroup_service.connection.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class UserRequestDto {
    private Integer accountBorrowId;
    private Integer userBorrowId;
    private BigDecimal amount;
    private String description;
}
