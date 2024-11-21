package com.billit.loangroup_service.connection.loan.dto;

import com.billit.loangroup_service.enums.RiskLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanResponseClientDto {
    private Integer loanId;
    private Integer groupId;
    private Integer userBorrowId;
    private Integer userBorrowAccountId;
    private BigDecimal loanAmount;
    private RiskLevel riskLevel;
    private Integer memberCount;
    private Boolean isFulled;
    private BigDecimal intRate;

    public LoanResponseClientDto(Integer loanId, Integer groupId, Integer userBorrowId, Integer userBorrowAccountId, BigDecimal loanAmount, RiskLevel riskLevel, Integer memberCount, Boolean isFulled, BigDecimal intRate) {
        this.loanId = loanId;
        this.groupId = groupId;
        this.userBorrowId = userBorrowId;
        this.userBorrowAccountId = userBorrowAccountId;
        this.loanAmount = loanAmount;
        this.riskLevel = riskLevel;
        this.memberCount = memberCount;
        this.isFulled = isFulled;
        this.intRate = intRate;
    }
}
