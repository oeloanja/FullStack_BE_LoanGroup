package com.billit.loangroup_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SettlementCalculationEvent {
    private Integer groupId;
    private String status;
}
