package com.billit.loangroup_service.connection.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoanStatusUpdateRequestDto {
    private Integer loanId;
    private int status;
}
