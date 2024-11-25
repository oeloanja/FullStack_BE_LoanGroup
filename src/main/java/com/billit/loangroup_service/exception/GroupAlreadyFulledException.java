package com.billit.loangroup_service.exception;

public class GroupAlreadyFulledException extends LoanGroupException {
    public GroupAlreadyFulledException(Integer groupId) {
        super("이미 모집이 완료된 그룹입니다. 그룹 ID: " + groupId);
    }
}