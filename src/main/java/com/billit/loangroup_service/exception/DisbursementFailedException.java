package com.billit.loangroup_service.exception;

public class DisbursementFailedException extends LoanGroupException {
    public DisbursementFailedException(Integer groupId) {
        super("대출금 입금 처리에 실패했습니다. 그룹 ID: " + groupId);
    }
}
