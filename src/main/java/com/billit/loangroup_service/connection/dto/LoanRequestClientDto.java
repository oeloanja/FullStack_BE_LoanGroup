package com.billit.loangroup_service.connection.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class LoanRequestClientDto {
    private Integer loanId;

    public LoanRequestClientDto(Integer loanId) {
        this.loanId = loanId;
    }
}
