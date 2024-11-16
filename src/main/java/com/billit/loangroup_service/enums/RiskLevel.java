package com.billit.loangroup_service.enums;

import java.math.BigDecimal;

public enum RiskLevel {
    LOW(10.0, 13.0),
    MEDIUM(13.01, 16.0),
    HIGH(16.01, 20.0);

    private final double minRate;
    private final double maxRate;

    RiskLevel(double minRate, double maxRate) {
        this.minRate = minRate;
        this.maxRate = maxRate;
    }

    public static RiskLevel fromInterestRate(BigDecimal interestRate) {
        double rate = interestRate.doubleValue();
        if (rate >= LOW.minRate && rate <= LOW.maxRate) return LOW;
        if (rate >= MEDIUM.minRate && rate <= MEDIUM.maxRate) return MEDIUM;
        if (rate >= HIGH.minRate && rate <= HIGH.maxRate) return HIGH;
        throw new IllegalArgumentException("Interest rate out of valid range: " + rate);
    }
}
