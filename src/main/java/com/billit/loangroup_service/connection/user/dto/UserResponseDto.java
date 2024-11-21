package com.billit.loangroup_service.connection.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private List<Boolean> success;  // 각 요청별 처리 결과
}