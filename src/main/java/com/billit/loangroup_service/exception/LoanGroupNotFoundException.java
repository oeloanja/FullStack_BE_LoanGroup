package com.billit.loangroup_service.exception;

public class LoanGroupNotFoundException extends LoanGroupException {
    public LoanGroupNotFoundException(Integer groupId) {
        super("존재하지 않는 대출 그룹입니다. 그룹 ID: " + groupId);
    }
}