package com.billit.loangroup_service.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Account related
    CLOSED_ACCOUNT("CLOSED_ACCOUNT_001", "이미 마감된 계좌에는 입금할 수 없습니다. 그룹명: %s", 400),

    // Disbursement related
    DISBURSEMENT_FAILED("DISBURSEMENT_001", "대출금 입금 처리에 실패했습니다. 그룹명: %s", 400),

    // Group related
    GROUP_ALREADY_FILLED("GROUP_001", "이미 모집이 완료된 그룹입니다. 그룹명: %s", 400),
    LOAN_GROUP_NOT_FOUND("GROUP_002", "존재하지 않는 대출 그룹입니다. 그룹명: %s", 404),

    // Loan related
    LOAN_NOT_FOUND("LOAN_001", "대출 정보를 찾을 수 없습니다. 대출 ID: %d", 404),

    // Common
    INVALID_PARAMETER("COMMON_001", "%s", 400);

    private final String code;
    private final String message;
    private final int status;

    public String getMessage(Object... args) {
        return String.format(message, args);
    }
}