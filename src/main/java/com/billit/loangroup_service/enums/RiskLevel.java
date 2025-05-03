package com.billit.loangroup_service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@AllArgsConstructor
public enum RiskLevel {
    LOW(0),
    MEDIUM(1),
    HIGH(2);

    private final int value;

    private static final BigDecimal MEDIUM_RISK_MIN_RATE = new BigDecimal("13.0");
    private static final BigDecimal MEDIUM_RISK_MAX_RATE = new BigDecimal("17.0");

    public static RiskLevel determineRiskLevel(BigDecimal adjustedRate) {
        if (adjustedRate.compareTo(MEDIUM_RISK_MIN_RATE) >= 0 &&
                adjustedRate.compareTo(MEDIUM_RISK_MAX_RATE) <= 0) {
            return MEDIUM;
        } else if (adjustedRate.compareTo(MEDIUM_RISK_MIN_RATE) < 0) {
            return LOW;
        } else {
            return HIGH;
        }
    }

    public static RiskLevel fromOrdinal(int value) {
        for (RiskLevel level : values()) {
            if (level.value == value) {
                return level;
            }
        }
        throw new IllegalArgumentException("Invalid risk level value: " + value);
    }
}