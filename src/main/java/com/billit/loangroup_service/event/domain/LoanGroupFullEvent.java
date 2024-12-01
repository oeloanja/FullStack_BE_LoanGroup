package com.billit.loangroup_service.event.domain;

import lombok.Getter;

@Getter
public class LoanGroupFullEvent {
    private final Integer groupId;

    public LoanGroupFullEvent(Integer groupId) {
        this.groupId = groupId;
    }
}
