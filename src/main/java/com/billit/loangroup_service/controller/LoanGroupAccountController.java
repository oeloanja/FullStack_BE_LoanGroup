package com.billit.loangroup_service.controller;


import com.billit.loangroup_service.connection.dto.InvestmentRequestDto;
import com.billit.loangroup_service.dto.LoanGroupResponseDto;
import com.billit.loangroup_service.service.LoanGroupAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Controller
@RequestMapping("/api/v1/platform")
public class LoanGroupAccountController {
    private final LoanGroupAccountService loanGroupAccountService;

    // 투자금 현황 업데이트
    @PutMapping("/invest")
    public ResponseEntity<String> updatePlatformAccountBalance(
            @RequestBody InvestmentRequestDto request) {
        loanGroupAccountService.updateLoanGroupAccountBalance(request.getGroupId(), request.getAmount());
        return ResponseEntity.ok().build();
    }
}
