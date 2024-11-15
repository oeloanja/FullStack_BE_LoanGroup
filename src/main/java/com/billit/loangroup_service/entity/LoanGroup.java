package com.billit.loangroup_service.entity;

import com.billit.loangroup_service.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class LoanGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId;

    @Column
    private String groupName;

    @Column
    @Enumerated(EnumType.ORDINAL)
    private RiskLevel riskLevel;

    private Double maxInterestRate;
    private Double minInterestRate;

    private Boolean isFulled = false;
    private LocalDateTime createdAt;

    @Transient
    public static final int MAX_MEMBERS = 10;

    private int memberCount = 0;

    public void addMember() {
        memberCount++;
    }
}
