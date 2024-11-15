package com.billit.loangroup_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "platform_account")
public class PlatformAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer platformAccountId;

    @OneToOne
    @JoinColumn(name = "group_id", nullable = false)
    private LoanGroup group;

    @Column(nullable = false, length = 50)
    private BigDecimal requiredAmount;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal currentBalance;

    @Column(nullable = false)
    private Boolean isClosed = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PlatformAccount(LoanGroup groupId, BigDecimal requiredAmount, BigDecimal currentBalance) {
        this.group = groupId;
        this.requiredAmount = requiredAmount;
        this.currentBalance = currentBalance;
        this.isClosed = false;
    }

    public void updateBalance(BigDecimal amount) {
        this.currentBalance = this.currentBalance.add(amount);
        if (this.currentBalance.compareTo(this.requiredAmount) >= 0) {
            this.isClosed = true;
        }
    }
}

