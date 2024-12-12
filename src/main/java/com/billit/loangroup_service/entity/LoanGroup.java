package com.billit.loangroup_service.entity;

import com.billit.loangroup_service.enums.RiskLevel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Entity
@Getter
public class LoanGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer groupId;

    @Column
    private String groupName;

    @Column
    @Enumerated(EnumType.ORDINAL)
    private RiskLevel riskLevel;

    private BigDecimal intRateAvg;

    private Boolean isFulled = false;
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "group", cascade = CascadeType.ALL)
    @JsonIgnore
    private LoanGroupAccount loanGroupAccount;


    @Transient
    public static final int MAX_MEMBERS = 3;

    private int memberCount = 0;

    public LoanGroup(String groupName, RiskLevel riskLevel, LocalDateTime createdAt) {
        this.groupName = groupName;
        this.riskLevel = riskLevel;
        this.createdAt = createdAt;
        this.isFulled = false;
        this.memberCount = 0;
        this.intRateAvg = BigDecimal.valueOf(0.0);
    }

    public void updateGroupAsFull(){
        this.isFulled = true;
    }

    public void incrementMemberCount() {
        this.memberCount++;
    }

    public void updateIntRateAvg(BigDecimal avg) {
        this.intRateAvg = avg;
    }
}
