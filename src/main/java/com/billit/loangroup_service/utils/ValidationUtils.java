package com.billit.loangroup_service.utils;

import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.entity.LoanGroupAccount;
import com.billit.loangroup_service.exception.ClosedAccountException;
import com.billit.loangroup_service.exception.LoanGroupNotFoundException;
import com.billit.loangroup_service.exception.LoanNotFoundException;
import com.billit.loangroup_service.connection.loan.dto.LoanResponseClientDto;

import java.util.List;
import java.util.Optional;

public class ValidationUtils {
    public static void validateLoanGroupExistence(Optional<LoanGroup> group, Integer groupId) {
        group.orElseThrow(() -> new LoanGroupNotFoundException(groupId));
    }

    public static void validateLoanExistence(List<LoanResponseClientDto> loans, Integer groupId) {
        if (loans.isEmpty()) {
            throw new LoanNotFoundException(groupId);
        }
    }

    public static void validateAccountNotClosed(LoanGroupAccount account) {
        if (account.getIsClosed()) {
            throw new ClosedAccountException(account.getLoanGroupAccountId());
        }
    }
}
