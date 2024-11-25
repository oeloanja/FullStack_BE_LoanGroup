package com.billit.loangroup_service.enums;

import lombok.Getter;

import java.math.BigDecimal;

public enum RiskLevel {
    LOW(0, 10.0, 13.0),
    MEDIUM(1, 13.01, 16.0),
    HIGH(2, 16.01, 20.0);

    @Getter
    private final int ordinal;
    private final double minRate;
    private final double maxRate;

    RiskLevel(int ordinal, double minRate, double maxRate) {
        this.ordinal = ordinal;
        this.minRate = minRate;
        this.maxRate = maxRate;
    }

    public static RiskLevel fromOrdinal(int ordinal) {
        for (RiskLevel level : values()) {
            if (level.ordinal == ordinal) return level;
        }
        throw new IllegalArgumentException("Invalid ordinal: " + ordinal);
    }

    public static RiskLevel fromInterestRate(BigDecimal interestRate) {
        double rate = interestRate.doubleValue();
        if (rate >= LOW.minRate && rate <= LOW.maxRate) return LOW;
        if (rate >= MEDIUM.minRate && rate <= MEDIUM.maxRate) return MEDIUM;
        if (rate >= HIGH.minRate && rate <= HIGH.maxRate) return HIGH;
        throw new IllegalArgumentException("Interest rate out of valid range: " + rate);
    }
}
