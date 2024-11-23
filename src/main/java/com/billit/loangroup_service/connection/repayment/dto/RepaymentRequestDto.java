package com.billit.loangroup_service.connection.repayment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentRequestDto {
    private Integer loanId;
    private Integer groupId;
    private BigDecimal loanAmount;
    private Integer term;
    private BigDecimal intRate;
    private LocalDate issueDate;
}
