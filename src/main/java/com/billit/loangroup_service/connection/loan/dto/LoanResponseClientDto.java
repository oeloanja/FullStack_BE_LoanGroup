package com.billit.loangroup_service.connection.loan.dto;

import com.billit.loangroup_service.enums.RiskLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanResponseClientDto {
    private Integer loanId;
    private Integer groupId;
    private UUID userBorrowId;
    private Integer term;
    private Integer accountBorrowId;
    private BigDecimal loanAmount;
    private LocalDate issueDate;
    private BigDecimal intRate;
}
