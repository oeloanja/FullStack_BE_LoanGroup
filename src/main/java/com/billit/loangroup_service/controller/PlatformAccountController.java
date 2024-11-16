package com.billit.loangroup_service.controller;


import com.billit.loangroup_service.dto.PlatformAccountRequestDto;
import com.billit.loangroup_service.dto.PlatformAccountResponseDto;
import com.billit.loangroup_service.service.LoanGroupService;
import com.billit.loangroup_service.service.PlatformAccountService;
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
public class PlatformAccountController {
    private final PlatformAccountService platformAccountService;

    @PutMapping("/{platformAccountId}/invest")
    public ResponseEntity<Void> processInvestment(
            @PathVariable Integer platformAccountId,
            @RequestBody BigDecimal amount) {
        platformAccountService.processInvestment(platformAccountId, amount);
        return ResponseEntity.ok().build();
    }
}
