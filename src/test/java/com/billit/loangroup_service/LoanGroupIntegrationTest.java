package com.billit.loangroup_service;

import com.billit.loangroup_service.config.AsyncConfig;
import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.enums.RiskLevel;
import com.billit.loangroup_service.repository.LoanGroupRepository;
import com.billit.loangroup_service.connection.loan.client.LoanServiceClient;
import com.billit.loangroup_service.connection.loan.dto.LoanResponseClientDto;
import com.billit.loangroup_service.service.LoanGroupService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
},
        classes = {AsyncConfig.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext
@Transactional
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
class LoanGroupIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoanGroupRepository loanGroupRepository;

    @MockBean
    private LoanServiceClient loanServiceClient;

    @Autowired
    private LoanGroupService loanGroupService;

    @BeforeEach
    void setUp() {
        loanGroupRepository.deleteAll();
    }

    @Test
    @DisplayName("동시에 여러 요청이 들어올 때 동시성 테스트")
    void concurrencyTest() throws Exception {
        // Given
        int numberOfThreads = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        LoanGroup initialGroup = new LoanGroup("GR-TEST", RiskLevel.MEDIUM, LocalDateTime.now());
        loanGroupRepository.save(initialGroup);

        LoanResponseClientDto mockLoanResponse = new LoanResponseClientDto(
                1,
                null,
                UUID.randomUUID(),
                12,
                1,
                new BigDecimal("10000000"),
                null,
                new BigDecimal("14.5"),
                new BigDecimal(3000000)
        );
        when(loanServiceClient.getLoanById(anyInt())).thenReturn(mockLoanResponse);

        // When
        List<Future<MvcResult>> futures = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    return mockMvc.perform(post("/api/loan-groups/assign")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"loanId\": 1}"))
                            .andReturn();
                } finally {
                    latch.countDown();
                }
            }));
        }

        latch.await(10, TimeUnit.SECONDS);

        // Then
        List<MvcResult> results = futures.stream()
                .map(future -> {
                    try {
                        return future.get(5, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        fail("Request failed: " + e.getMessage());
                        return null;
                    }
                })
                .toList();

        // Verify
        LoanGroup updatedGroup = loanGroupRepository.findById(Long.valueOf(initialGroup.getGroupId())).orElseThrow();
        assertTrue(updatedGroup.getMemberCount() <= LoanGroup.MAX_MEMBERS,
                "Member count should not exceed max members");

        long successCount = results.stream()
                .filter(result -> result.getResponse().getStatus() == 200)
                .count();
        assertTrue(successCount <= LoanGroup.MAX_MEMBERS,
                "Successful assignments should not exceed max members");
    }

    @Test
    @DisplayName("그룹 풀 관리 테스트")
    void groupPoolManagementTest() throws Exception {
        // Given
        RiskLevel testRiskLevel = RiskLevel.LOW;
        IntStream.range(0, 2).forEach(i -> {
            LoanGroup group = new LoanGroup("GR-TEST-" + i, testRiskLevel, LocalDateTime.now());
            loanGroupRepository.save(group);
        });
        loanGroupRepository.flush();

        // When
        loanGroupService.checkAndReplenishGroupPool(testRiskLevel);

        // Then
        int finalEmptyGroups = loanGroupRepository
                .countByRiskLevelAndMemberCountAndIsFullFalse(testRiskLevel.getValue());
        assertTrue(finalEmptyGroups >= 3,
                "Should maintain minimum number of empty groups");
    }
}