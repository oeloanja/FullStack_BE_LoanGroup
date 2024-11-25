package com.billit.loangroup_service.event.domain;

import com.billit.loangroup_service.entity.LoanGroup;
import lombok.Getter;

@Getter
public class LoanGroupFullEvent {
    private final Integer groupId;

    public LoanGroupFullEvent(Integer groupId) {
        this.groupId = groupId;
    }
}
