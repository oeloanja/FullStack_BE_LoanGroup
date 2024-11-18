package com.billit.loangroup_service.controller;


import com.billit.loangroup_service.service.LoanGroupAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Controller
@RequestMapping("/api/v1/platform")
public class LoanGroupAccountController {
    private final LoanGroupAccountService loanGroupAccountService;

    // 투자금 현황 업데이트
    @PutMapping("/{platformAccountId}/invest")
    public ResponseEntity<Void> updatePlatformAccountBalance(
            @PathVariable Integer platformAccountId,
            @RequestBody BigDecimal amount) {
        loanGroupAccountService.updatePlatformAccountBalance(platformAccountId, amount);
        return ResponseEntity.ok().build();
    }
}
