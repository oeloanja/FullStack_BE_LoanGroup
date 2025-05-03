package com.billit.loangroup_service.enums;

import java.math.BigDecimal;

public record RiskLevelResult(
        RiskLevel riskLevel,
        BigDecimal adjustedRate
) {}