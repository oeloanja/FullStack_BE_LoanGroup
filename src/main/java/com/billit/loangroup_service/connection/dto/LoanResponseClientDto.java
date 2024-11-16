package com.billit.loangroup_service.connection.dto;

import com.billit.loangroup_service.enums.RiskLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class LoanResponseClientDto {
    private Long groupId;
    private RiskLevel riskLevel;
    private Integer memberCount;
    private Boolean isFulled;
    private BigDecimal loanAmount;
    private BigDecimal intRate;

    public LoanResponseClientDto(Long groupId, RiskLevel riskLevel, Integer memberCount, Boolean isFilled, BigDecimal loanAmount, BigDecimal intRate) {
        this.groupId = groupId;
        this.riskLevel = riskLevel;
        this.memberCount = memberCount;
        this.isFulled = isFilled;
        this.loanAmount = loanAmount;
        this.intRate = intRate;
    }
}
