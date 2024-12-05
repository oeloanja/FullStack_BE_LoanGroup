package com.billit.loangroup_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanResponseClientEventDto {
    private Integer loanId;
    private Integer groupId;
    private Integer accountBorrowId;
    private Integer userBorrowId;
    private BigDecimal loanAmount;
    private Integer term;
    private BigDecimal intRate;
}