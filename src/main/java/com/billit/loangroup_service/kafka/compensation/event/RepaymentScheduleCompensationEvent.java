package com.billit.loangroup_service.kafka.compensation.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RepaymentScheduleCompensationEvent {
    private Integer groupId;
}
