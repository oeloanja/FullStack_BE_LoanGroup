package com.billit.loangroup_service.kafka.compensation.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoanStatusCompensationEvent {
    private Integer loanId;
}
