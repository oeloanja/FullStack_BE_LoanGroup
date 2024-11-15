package com.billit.loangroup_service.dto;

import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.enums.RiskLevel;
import lombok.Getter;

import static com.billit.loangroup_service.entity.LoanGroup.calculateInterest;

@Getter
public class LoanGroupResponseDto {
    private final Integer groupId;
    private final String groupName;
    private final Double InterestRate;
    private final RiskLevel riskLevel;
    private final Boolean isFulled;

    public LoanGroupResponseDto(LoanGroup loanGroup) {
        this.groupId = loanGroup.getGroupId();
        this.groupName = loanGroup.getGroupName();
        this.InterestRate = calculateInterest(loanGroup);
        this.riskLevel = loanGroup.getRiskLevel();
        this.isFulled = loanGroup.getIsFulled();
    }

    public static LoanGroupResponseDto from(LoanGroup loanGroup) {
        return new LoanGroupResponseDto(loanGroup);
    }
}
