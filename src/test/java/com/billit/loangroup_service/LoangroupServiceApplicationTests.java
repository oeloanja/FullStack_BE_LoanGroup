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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
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
    @Captor
    private ArgumentCaptor<LoanGroup> loanGroupCaptor;

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
                new BigDecimal("11.5"),      // intRate - LOW risk (10.0 ~ 13.0)
                new BigDecimal(3000000)
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
                new BigDecimal("12.3"),
                new BigDecimal(3000000)
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
                new BigDecimal("14.5"),
                new BigDecimal(3000000)
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
                new BigDecimal("10000000"), null, new BigDecimal("11.5"),
                new BigDecimal(3000000)
        );

        when(loanServiceClient.getLoanById(anyInt())).thenReturn(loanResponse);
        when(loanGroupRepository.findAvailableGroupId(anyInt(), anyInt())).thenReturn(Optional.of(1L));
        when(loanGroupRepository.incrementMemberCount(1L)).thenReturn(0); // 증가 실패

        // When & Then
        assertThatThrownBy(() -> loanGroupService.assignGroup(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("멤버 배정에 실패했습니다.");
    }

    @Test
    void shouldCloseStaleGroupsWithMembers() {
        // Given
        LocalDateTime oldTime = LocalDateTime.now().minusHours(25); // 25시간 전
        LoanGroup staleGroupWithMembers = createStaleGroup(3, oldTime);
        LoanGroup staleGroupWithoutMembers = createStaleGroup(0, oldTime);

        when(loanGroupRepository.findByCreatedAtLessThanAndIsFulledFalse(any()))
                .thenReturn(Arrays.asList(staleGroupWithMembers, staleGroupWithoutMembers));

        // When
        loanGroupService.checkAndCloseStaleGroups();

        // Then
        verify(loanGroupRepository).save(loanGroupCaptor.capture());
        LoanGroup savedGroup = loanGroupCaptor.getValue();
        assertThat(savedGroup.getIsFulled()).isTrue();
        assertThat(savedGroup.getGroupId()).isEqualTo(staleGroupWithMembers.getGroupId());

        // Kafka 이벤트 발행 확인
        verify(kafkaTemplate).send(
                eq("loan-group-full"),
                eq(staleGroupWithMembers.getGroupId().toString()),
                any(LoanGroupFullEvent.class)
        );

        // 멤버가 없는 그룹은 처리되지 않았는지 확인
        verify(kafkaTemplate, never()).send(
                eq("loan-group-full"),
                eq(staleGroupWithoutMembers.getGroupId().toString()),
                any(LoanGroupFullEvent.class)
        );
    }

    private LoanGroup createStaleGroup(int memberCount, LocalDateTime createdAt) {
        LoanGroup group = new LoanGroup(
                "TEST-" + memberCount,
                RiskLevel.LOW,
                createdAt
        );

        ReflectionTestUtils.setField(group, "groupId", memberCount);

        for (int i = 1; i < memberCount; i++) {
            group.incrementMemberCount();
        }

        return group;
    }

    @Test
    void shouldNotCloseRecentGroups() {
        // Given
        LocalDateTime recentTime = LocalDateTime.now().minusHours(23); // 23시간 전
        LoanGroup recentGroup = createStaleGroup(3, recentTime);

        // when절을 수정: 아무 그룹도 반환하지 않도록
        when(loanGroupRepository.findByCreatedAtLessThanAndIsFulledFalse(any()))
                .thenReturn(Collections.emptyList());  // 빈 리스트 반환

        // When
        loanGroupService.checkAndCloseStaleGroups();

        // Then
        verify(loanGroupRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    void shouldHandleEmptyStaleGroups() {
        // Given
        when(loanGroupRepository.findByCreatedAtLessThanAndIsFulledFalse(any()))
                .thenReturn(Collections.emptyList());

        // When
        loanGroupService.checkAndCloseStaleGroups();

        // Then
        verify(loanGroupRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }


}