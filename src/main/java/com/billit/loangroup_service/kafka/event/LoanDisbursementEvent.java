package com.billit.loangroup_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanDisbursementEvent {
    private List<LoanResponseClientEventDto> groupLoans;
    private Integer groupId;
    private String status;
}
