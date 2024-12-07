package com.billit.loangroup_service.exception;

import lombok.Getter;

@Getter
public class ErrorResponse {
    private final String code;
    private final String message;
    private final int status;

    public ErrorResponse(CustomException e) {
        this.code = e.getErrorCode().getCode();
        this.message = e.getMessage();
        this.status = e.getStatus();
    }
}
