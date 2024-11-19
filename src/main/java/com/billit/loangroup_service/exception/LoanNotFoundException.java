package com.billit.loangroup_service.exception;

public class LoanNotFoundException extends LoanGroupException {
    public LoanNotFoundException(Integer loanId) {
        super("대출 정보를 찾을 수 없습니다. 대출 ID: " + loanId);
    }
}