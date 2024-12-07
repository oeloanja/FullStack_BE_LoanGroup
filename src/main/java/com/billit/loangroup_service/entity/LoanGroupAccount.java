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
@Table(name = "loan_group_account")
public class LoanGroupAccount implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer LoanGroupAccountId;

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

    public LoanGroupAccount(LoanGroup group, BigDecimal requiredAmount, BigDecimal currentBalance, LocalDateTime createdAt) {
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

    public void closeAccount(){
        this.isClosed = true;
    }
}

