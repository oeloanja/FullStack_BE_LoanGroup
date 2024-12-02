package com.billit.loangroup_service.connection.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoanSuccessStatusRequestDto {
    private Integer loanId;
    private int status;
    private LocalDate issueDate;
}
