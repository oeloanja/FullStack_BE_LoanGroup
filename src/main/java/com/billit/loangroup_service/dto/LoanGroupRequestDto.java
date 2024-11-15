package com.billit.loangroup_service.dto;

import com.billit.loangroup_service.enums.RiskLevel;
import lombok.Getter;

@Getter
public class LoanGroupRequestDto {
    private RiskLevel riskLevel;
}
