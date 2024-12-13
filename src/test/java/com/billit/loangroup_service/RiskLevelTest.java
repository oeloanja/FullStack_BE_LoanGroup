package com.billit.loangroup_service;

import com.billit.loangroup_service.enums.RiskLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class RiskLevelTest {
    private static final BigDecimal BASE_LOW_RATE = new BigDecimal("10.0");
    private static final BigDecimal BASE_HIGH_RATE = new BigDecimal("15.0");
    private static final BigDecimal LOAN_LIMIT_3M = new BigDecimal("3000000");
    private static final BigDecimal LOAN_LIMIT_5M = new BigDecimal("5000000");

    @Nested
    @DisplayName("500만원 한도 신용우수(10%) 대출 테스트")
    class FiveMillionLimitTests {
        @ParameterizedTest(name = "한도사용률 {0}% 일 때 이율 {1}%로 계산되어 {2} 리스크")
        @CsvSource({
                "20, 10.0, LOW",    // 40% 이하 사용 - 기본 이율 유지
                "40, 10.0, LOW",    // 40% 사용 - 기본 이율 유지
                "60, 11.7, LOW",    // 60% 사용 -> 10 + ((0.6-0.4) * 8.333333) = 11.7%
                "80, 13.3, MEDIUM", // 80% 사용 -> 10 + ((0.8-0.4) * 8.333333) = 13.3%
                "100, 15.0, MEDIUM" // 100% 사용 -> 10 + ((1.0-0.4) * 8.333333) = 15.0%
        })
        void shouldCalculateCorrectRiskLevelForLowBaseRate(
                int usagePercent, double expectedRate, RiskLevel expectedRisk) {
            BigDecimal requestAmount = LOAN_LIMIT_5M.multiply(new BigDecimal(usagePercent))
                    .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);

            RiskLevel result = RiskLevel.calculateRiskLevel(BASE_LOW_RATE, requestAmount, LOAN_LIMIT_5M);
            assertEquals(expectedRisk, result);
        }

        @Test
        @DisplayName("한도사용률 상승에 따른 이율 증가 검증")
        void shouldIncreaseRateProperlyForLowBaseRate() {
            BigDecimal fortyPercentAmount = LOAN_LIMIT_5M.multiply(new BigDecimal("0.4"));
            BigDecimal sixtyPercentAmount = LOAN_LIMIT_5M.multiply(new BigDecimal("0.6"));
            BigDecimal eightyPercentAmount = LOAN_LIMIT_5M.multiply(new BigDecimal("0.8"));

            assertEquals(RiskLevel.LOW, RiskLevel.calculateRiskLevel(
                    BASE_LOW_RATE, fortyPercentAmount, LOAN_LIMIT_5M));  // 10.0%
            assertEquals(RiskLevel.LOW, RiskLevel.calculateRiskLevel(
                    BASE_LOW_RATE, sixtyPercentAmount, LOAN_LIMIT_5M));  // 11.7%
            assertEquals(RiskLevel.MEDIUM, RiskLevel.calculateRiskLevel(
                    BASE_LOW_RATE, eightyPercentAmount, LOAN_LIMIT_5M)); // 13.3%
        }
    }

    @Nested
    @DisplayName("300만원 한도 신용보통(15%) 대출 테스트")
    class ThreeMillionLimitTests {
        @ParameterizedTest(name = "한도사용률 {0}% 일 때 이율 {1}%로 계산되어 {2} 리스크")
        @CsvSource({
                "20, 15.0, MEDIUM",  // 40% 이하 사용 - 기본 이율 유지
                "40, 15.0, MEDIUM",  // 40% 사용 - 기본 이율 유지
                "60, 16.7, MEDIUM",  // 60% 사용 -> 15 + ((0.6-0.4) * 8.333333) = 16.7%
                "80, 18.3, HIGH",    // 80% 사용 -> 15 + ((0.8-0.4) * 8.333333) = 18.3%
                "100, 20.0, HIGH"    // 100% 사용 -> 15 + ((1.0-0.4) * 8.333333) = 20.0%
        })
        void shouldCalculateCorrectRiskLevelForHighBaseRate(
                int usagePercent, double expectedRate, RiskLevel expectedRisk) {
            BigDecimal requestAmount = LOAN_LIMIT_3M.multiply(new BigDecimal(usagePercent))
                    .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);

            RiskLevel result = RiskLevel.calculateRiskLevel(BASE_HIGH_RATE, requestAmount, LOAN_LIMIT_3M);
            assertEquals(expectedRisk, result);
        }

        @Test
        @DisplayName("한도사용률 상승에 따른 이율 증가 검증")
        void shouldIncreaseRateProperlyForHighBaseRate() {
            BigDecimal fortyPercentAmount = LOAN_LIMIT_3M.multiply(new BigDecimal("0.4"));
            BigDecimal sixtyPercentAmount = LOAN_LIMIT_3M.multiply(new BigDecimal("0.6"));
            BigDecimal eightyPercentAmount = LOAN_LIMIT_3M.multiply(new BigDecimal("0.8"));

            assertEquals(RiskLevel.MEDIUM, RiskLevel.calculateRiskLevel(
                    BASE_HIGH_RATE, fortyPercentAmount, LOAN_LIMIT_3M));  // 15.0%
            assertEquals(RiskLevel.MEDIUM, RiskLevel.calculateRiskLevel(
                    BASE_HIGH_RATE, sixtyPercentAmount, LOAN_LIMIT_3M));  // 16.7%
            assertEquals(RiskLevel.HIGH, RiskLevel.calculateRiskLevel(
                    BASE_HIGH_RATE, eightyPercentAmount, LOAN_LIMIT_3M)); // 18.3%
        }
    }

    @Nested
    @DisplayName("한도 사용률 40% 기준 경계값 테스트")
    class ThresholdTests {
        @Test
        @DisplayName("500만원 한도에서 40% 경계값 전후 테스트")
        void shouldHandleFiveMillionLimitThreshold() {
            BigDecimal thresholdAmount = LOAN_LIMIT_5M.multiply(new BigDecimal("0.4"));

            // 정확히 40%
            assertEquals(RiskLevel.LOW, RiskLevel.calculateRiskLevel(
                    BASE_LOW_RATE, thresholdAmount, LOAN_LIMIT_5M));

            // 40% + 1원
            assertEquals(RiskLevel.LOW, RiskLevel.calculateRiskLevel(
                    BASE_LOW_RATE, thresholdAmount.add(BigDecimal.ONE), LOAN_LIMIT_5M));
        }

        @Test
        @DisplayName("300만원 한도에서 40% 경계값 전후 테스트")
        void shouldHandleThreeMillionLimitThreshold() {
            BigDecimal thresholdAmount = LOAN_LIMIT_3M.multiply(new BigDecimal("0.4"));

            // 정확히 40%
            assertEquals(RiskLevel.MEDIUM, RiskLevel.calculateRiskLevel(
                    BASE_HIGH_RATE, thresholdAmount, LOAN_LIMIT_3M));

            // 40% + 1원
            assertEquals(RiskLevel.MEDIUM, RiskLevel.calculateRiskLevel(
                    BASE_HIGH_RATE, thresholdAmount.add(BigDecimal.ONE), LOAN_LIMIT_3M));
        }
    }

    @Nested
    @DisplayName("유효성 검사 테스트")
    class ValidationTests {
        @Test
        @DisplayName("요청 금액이 0 이하인 경우")
        void shouldReturnHighRiskWhenAmountIsInvalid() {
            assertEquals(RiskLevel.HIGH, RiskLevel.calculateRiskLevel(
                    BASE_LOW_RATE, BigDecimal.ZERO, LOAN_LIMIT_5M));
            assertEquals(RiskLevel.HIGH, RiskLevel.calculateRiskLevel(
                    BASE_HIGH_RATE, new BigDecimal("-1"), LOAN_LIMIT_3M));
        }

        @Test
        @DisplayName("한도 초과 요청인 경우")
        void shouldReturnHighRiskWhenExceedingLimit() {
            assertEquals(RiskLevel.HIGH, RiskLevel.calculateRiskLevel(
                    BASE_LOW_RATE, LOAN_LIMIT_5M.add(BigDecimal.ONE), LOAN_LIMIT_5M));
            assertEquals(RiskLevel.HIGH, RiskLevel.calculateRiskLevel(
                    BASE_HIGH_RATE, LOAN_LIMIT_3M.add(BigDecimal.ONE), LOAN_LIMIT_3M));
        }

        @Test
        @DisplayName("한도와 이율이 매칭되지 않는 경우")
        void shouldReturnHighRiskWhenRateAndLimitDoNotMatch() {
            assertEquals(RiskLevel.HIGH, RiskLevel.calculateRiskLevel(
                    BASE_LOW_RATE, new BigDecimal("1000000"), LOAN_LIMIT_3M));
            assertEquals(RiskLevel.HIGH, RiskLevel.calculateRiskLevel(
                    BASE_HIGH_RATE, new BigDecimal("1000000"), LOAN_LIMIT_5M));
        }
    }
}