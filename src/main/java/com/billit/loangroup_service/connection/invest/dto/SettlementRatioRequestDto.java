package com.billit.loangroup_service.connection.invest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SettlementRatioRequestDto {
    Integer groupId;

    public SettlementRatioRequestDto(Integer groupId) {
        this.groupId = groupId;
    }
}
