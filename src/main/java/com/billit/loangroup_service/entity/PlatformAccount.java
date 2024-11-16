package com.billit.loangroup_service.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Getter
@NoArgsConstructor
@Entity
@JsonSerialize
@JsonDeserialize
@Table(name = "platform_account")
public class PlatformAccount implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer platformAccountId;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "group_id", nullable = false, unique = true)
    private LoanGroup group;

    @Column(nullable = false, length = 50)
    private BigDecimal requiredAmount;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal currentBalance;

    @Column(nullable = false)
    private Boolean isClosed = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    public PlatformAccount(LoanGroup group, BigDecimal requiredAmount, BigDecimal currentBalance, LocalDateTime createdAt) {
        this.group = group;
        this.requiredAmount = requiredAmount;
        this.currentBalance = currentBalance;
        this.isClosed = false;
        this.createdAt = createdAt;
    }

    public void updateBalance(BigDecimal amount) {
        this.currentBalance = this.currentBalance.add(amount);
        this.lastUpdatedAt = LocalDateTime.now();

        if (this.currentBalance.compareTo(this.requiredAmount) >= 0) {
            this.isClosed = true;
        }
    }

    public static void handleAccountClosure(PlatformAccount account) {
        log.info("Platform account {} is closed. Required amount: {}, Current balance: {}",
                account.getPlatformAccountId(),
                account.getRequiredAmount(),
                account.getCurrentBalance());

        // TODO: 추가적인 마감 처리 로직
        // 예: 대출 실행 요청, 투자자 알림 등
    }
}

