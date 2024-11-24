package com.billit.loangroup_service.exception;

public class ClosedAccountException extends LoanGroupException {
    public ClosedAccountException(Integer accountId) {
        super("이미 마감된 계좌에는 입금할 수 없습니다. 계좌 ID: " + accountId);
    }
}