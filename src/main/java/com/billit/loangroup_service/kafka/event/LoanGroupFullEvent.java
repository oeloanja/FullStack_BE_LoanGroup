package com.billit.loangroup_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanGroupFullEvent {
    private Integer groupId;
}
