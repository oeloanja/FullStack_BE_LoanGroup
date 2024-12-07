package com.billit.loangroup_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanResponseClientEventDto {
    private Integer loanId;
    private Integer groupId;
    private Integer accountBorrowId;
    private UUID userBorrowId;
    private BigDecimal loanAmount;
    private Integer term;
    private BigDecimal intRate;
}