package com.billit.loangroup_service.dto;

import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.enums.RiskLevel;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class LoanGroupResponseDto {
    private final Integer groupId;
    private final String groupName;
    private final BigDecimal intRate;
    private final RiskLevel riskLevel;
    private final Boolean isFulled;

    public LoanGroupResponseDto(LoanGroup loanGroup) {
        this.groupId = loanGroup.getGroupId();
        this.groupName = loanGroup.getGroupName();
        this.intRate = loanGroup.getIntRateAvg();
        this.riskLevel = loanGroup.getRiskLevel();
        this.isFulled = loanGroup.getIsFulled();
    }

    public static LoanGroupResponseDto from(LoanGroup loanGroup) {
        return new LoanGroupResponseDto(loanGroup);
    }
}
