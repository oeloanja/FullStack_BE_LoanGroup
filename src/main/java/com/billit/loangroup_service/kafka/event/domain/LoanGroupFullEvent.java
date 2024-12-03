package com.billit.loangroup_service.kafka.event.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoanGroupFullEvent {
    private Integer groupId;

    public LoanGroupFullEvent(Integer groupId) {
        this.groupId = groupId;
    }
}
