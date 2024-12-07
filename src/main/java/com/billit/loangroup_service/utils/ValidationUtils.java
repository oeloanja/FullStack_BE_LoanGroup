package com.billit.loangroup_service.utils;

import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.entity.LoanGroupAccount;
import com.billit.loangroup_service.connection.loan.dto.LoanResponseClientDto;

import java.util.List;
import java.util.Optional;
import com.billit.loangroup_service.exception.CustomException;
import com.billit.loangroup_service.exception.ErrorCode;

public class ValidationUtils {
    public static void validateLoanGroupExistence(Optional<LoanGroup> group, Integer groupId) {
        if (group.isEmpty()) {
            throw new CustomException(ErrorCode.LOAN_GROUP_NOT_FOUND, "Unknown");
        }
    }

    public static void validateLoanExistence(List<LoanResponseClientDto> loans) {
        if (loans.isEmpty()) {
            throw new CustomException(ErrorCode.LOAN_NOT_FOUND);
        }
    }

    public static void validateLoanExistence(LoanResponseClientDto loan, Integer loanId) {
        if (loan == null) {
            throw new CustomException(ErrorCode.LOAN_NOT_FOUND, loanId);
        }
    }

    public static void validateAccountNotClosed(LoanGroupAccount account) {
        if (account.getIsClosed()) {
            throw new CustomException(ErrorCode.CLOSED_ACCOUNT, account.getGroup().getGroupName());
        }
    }

    public static void validateGroupNotFull(LoanGroup group) {
        if (group.getIsFulled()) {
            throw new CustomException(ErrorCode.GROUP_ALREADY_FILLED, group.getGroupName());
        }
    }
}