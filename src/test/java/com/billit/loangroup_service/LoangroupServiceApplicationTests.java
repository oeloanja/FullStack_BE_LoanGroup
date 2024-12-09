package com.billit.loangroup_service;

import com.billit.loangroup_service.connection.loan.client.LoanServiceClient;
import com.billit.loangroup_service.connection.loan.dto.LoanRequestClientDto;
import com.billit.loangroup_service.connection.loan.dto.LoanResponseClientDto;
import com.billit.loangroup_service.dto.LoanGroupResponseDto;
import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.enums.RiskLevel;
import com.billit.loangroup_service.kafka.event.LoanGroupFullEvent;
import com.billit.loangroup_service.repository.LoanGroupRepository;
import com.billit.loangroup_service.service.LoanGroupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class LoangroupServiceApplicationTests {
    @Mock
    private LoanGroupRepository loanGroupRepository;
    @Mock
    private LoanServiceClient loanServiceClient;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    @InjectMocks
    private LoanGroupService loanGroupService;

    @Test
    @DisplayName("대출 그룹 배정 - 정상 케이스 (LOW 리스크)")
    void assignGroup_Success() {
        // Given
        LoanRequestClientDto request = new LoanRequestClientDto(1);
        LoanResponseClientDto loanResponse = new LoanResponseClientDto(
                1,                          // loanId
                null,                       // groupId
                UUID.randomUUID(),          // userBorrowId
                12,                         // term
                1,                          // accountBorrowId
                new BigDecimal("10000000"), // loanAmount
                null,                       // issueDate
                new BigDecimal("11.5")      // intRate - LOW risk (10.0 ~ 13.0)
        );

        LoanGroup group = new LoanGroup("GR123", RiskLevel.LOW, LocalDateTime.now());
        ReflectionTestUtils.setField(group, "groupId", 1);

        when(loanServiceClient.getLoanById(anyInt())).thenReturn(loanResponse);
        when(loanGroupRepository.findAvailableGroupId(anyInt(), anyInt())).thenReturn(Optional.of(1L));
        when(loanGroupRepository.incrementMemberCount(anyLong())).thenReturn(1);
        when(loanGroupRepository.findById(1L)).thenReturn(Optional.of(group));

        // When
        LoanGroupResponseDto result = loanGroupService.assignGroup(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getGroupName()).isEqualTo("GR123");
        verify(loanGroupRepository).findAvailableGroupId(anyInt(), anyInt());
        verify(loanGroupRepository).incrementMemberCount(1L);
        verify(loanGroupRepository).findById(1L);
    }

    @Test
    @DisplayName("대출 그룹 배정 - 가능한 그룹이 없는 경우")
    void assignGroup_WhenNoGroupAvailable() {
        // Given
        LoanRequestClientDto request = new LoanRequestClientDto(1);
        LoanResponseClientDto loanResponse = new LoanResponseClientDto(
                1,
                null,
                UUID.randomUUID(),
                12,
                1,
                new BigDecimal("10000000"),
                null,
                new BigDecimal("12.3")
        );

        when(loanServiceClient.getLoanById(anyInt())).thenReturn(loanResponse);
        when(loanGroupRepository.findAvailableGroupId(anyInt(), anyInt())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> loanGroupService.assignGroup(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("배정 가능한 그룹이 없습니다.");
    }

    @Test
    @DisplayName("대출 그룹 배정 - 그룹이 가득 찬 경우")
    void assignGroup_WhenGroupBecomesFull() {
        // Given
        LoanRequestClientDto request = new LoanRequestClientDto(1);
        LoanResponseClientDto loanResponse = new LoanResponseClientDto(
                1,
                null,
                UUID.randomUUID(),
                12,
                1,
                new BigDecimal("10000000"),
                null,
                new BigDecimal("14.5")
        );

        LoanGroup group = new LoanGroup("GR123", RiskLevel.MEDIUM, LocalDateTime.now());
        ReflectionTestUtils.setField(group, "groupId", 1);

        for (int i = 0; i < LoanGroup.MAX_MEMBERS - 1; i++) {
            group.incrementMemberCount();
        }

        when(loanServiceClient.getLoanById(anyInt())).thenReturn(loanResponse);
        when(loanGroupRepository.findAvailableGroupId(eq(RiskLevel.MEDIUM.ordinal()), eq(LoanGroup.MAX_MEMBERS)))
                .thenReturn(Optional.of(1L));
        when(loanGroupRepository.incrementMemberCount(1L)).thenReturn(1);
        when(loanGroupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(loanGroupRepository.saveAndFlush(any(LoanGroup.class))).thenReturn(group);

        // When
        LoanGroupResponseDto result = loanGroupService.assignGroup(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getGroupName()).isEqualTo("GR123");
        verify(kafkaTemplate).send(
                eq("loan-group-full"),
                eq("1"),
                any(LoanGroupFullEvent.class)
        );
        verify(loanGroupRepository).saveAndFlush(any(LoanGroup.class));
    }

    @Test
    @DisplayName("대출 그룹 배정 - 멤버 카운트 증가 실패")
    void assignGroup_WhenIncrementMemberCountFails() {
        // Given
        LoanRequestClientDto request = new LoanRequestClientDto(1);
        LoanResponseClientDto loanResponse = new LoanResponseClientDto(
                1, null, UUID.randomUUID(), 12, 1,
                new BigDecimal("10000000"), null, new BigDecimal("11.5")
        );

        when(loanServiceClient.getLoanById(anyInt())).thenReturn(loanResponse);
        when(loanGroupRepository.findAvailableGroupId(anyInt(), anyInt())).thenReturn(Optional.of(1L));
        when(loanGroupRepository.incrementMemberCount(1L)).thenReturn(0); // 증가 실패

        // When & Then
        assertThatThrownBy(() -> loanGroupService.assignGroup(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("멤버 배정에 실패했습니다.");
    }
}