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

    private static final BigDecimal BASE_LOW_RATE = new BigDecimal("10.0");
    private static final BigDecimal BASE_HIGH_RATE = new BigDecimal("15.0");
    private static final BigDecimal RATE_INCREASE_THRESHOLD = new BigDecimal("0.4");
    private static final BigDecimal MEDIUM_RISK_MIN_RATE = new BigDecimal("13.0");
    private static final BigDecimal MEDIUM_RISK_MAX_RATE = new BigDecimal("17.0");
    private static final BigDecimal LOAN_LIMIT_3M = new BigDecimal("3000000");
    private static final BigDecimal LOAN_LIMIT_5M = new BigDecimal("5000000");

    private final int value;

    public static RiskLevelResult calculateRiskLevel(BigDecimal baseInterestRate, BigDecimal requestAmount, BigDecimal baseLoanLimit) {
        if (requestAmount.compareTo(BigDecimal.ZERO) <= 0 || baseLoanLimit.compareTo(BigDecimal.ZERO) <= 0) {
            return new RiskLevelResult(HIGH, baseInterestRate);
        }

        if (requestAmount.compareTo(baseLoanLimit) > 0) {
            return new RiskLevelResult(HIGH, baseInterestRate);
        }

        boolean isValidCombination = (baseLoanLimit.compareTo(LOAN_LIMIT_3M) == 0 && baseInterestRate.compareTo(BASE_HIGH_RATE) == 0) ||
                (baseLoanLimit.compareTo(LOAN_LIMIT_5M) == 0 && baseInterestRate.compareTo(BASE_LOW_RATE) == 0);

        if (!isValidCombination) {
            return new RiskLevelResult(HIGH, baseInterestRate);
        }

        try {
            BigDecimal adjustedRate = getAdjustedRate(baseInterestRate, requestAmount, baseLoanLimit);

            if (adjustedRate.compareTo(MEDIUM_RISK_MIN_RATE) >= 0 &&
                    adjustedRate.compareTo(MEDIUM_RISK_MAX_RATE) <= 0) {
                return new RiskLevelResult(MEDIUM, adjustedRate);
            }
            return new RiskLevelResult(
                    adjustedRate.compareTo(MEDIUM_RISK_MIN_RATE) < 0 ? LOW : HIGH,
                    adjustedRate
            );

        } catch (ArithmeticException e) {
            return new RiskLevelResult(HIGH, baseInterestRate);
        }
    }

    private static BigDecimal getAdjustedRate(BigDecimal baseInterestRate, BigDecimal requestAmount, BigDecimal baseLoanLimit) {
        BigDecimal amountRatio = requestAmount.divide(baseLoanLimit, 4, RoundingMode.HALF_UP);
        BigDecimal adjustedRate;

        if (amountRatio.compareTo(RATE_INCREASE_THRESHOLD) <= 0) {
            adjustedRate = baseInterestRate;
        } else {
            BigDecimal excessRatio = amountRatio.subtract(RATE_INCREASE_THRESHOLD);
            BigDecimal scaledIncrease = excessRatio.multiply(new BigDecimal("8.333333"))
                    .setScale(4, RoundingMode.HALF_UP);
            adjustedRate = baseInterestRate.add(scaledIncrease);
        }

        adjustedRate = adjustedRate.setScale(1, RoundingMode.HALF_UP);
        return adjustedRate;
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