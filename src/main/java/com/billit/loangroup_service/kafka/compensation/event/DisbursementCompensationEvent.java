package com.billit.loangroup_service.kafka.compensation.event;

import com.billit.loangroup_service.enums.DisbursementStep;
import com.billit.loangroup_service.kafka.event.LoanResponseClientEventDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class DisbursementCompensationEvent {
    private Integer groupId;
    private DisbursementStep failedStep;
    private List<LoanResponseClientEventDto> groupLoans;
    private BigDecimal lastInvestAmount;
}
