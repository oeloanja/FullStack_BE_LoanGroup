package com.billit.loangroup_service.controller;

import com.billit.loangroup_service.connection.invest.dto.InvestmentRequestDto;
import com.billit.loangroup_service.dto.LoanGroupAccountResponseDto;
import com.billit.loangroup_service.service.LoanGroupAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/loans/group/account")
public class LoanGroupAccountController {
    private final LoanGroupAccountService loanGroupAccountService;

    // 투자금 현황 업데이트
    @PutMapping("/invest")
    public ResponseEntity<String> updatePlatformAccountBalance(
            @RequestBody InvestmentRequestDto request) {
        loanGroupAccountService.updateLoanGroupAccountBalance(request.getGroupId(), request.getAmount());
        return ResponseEntity.ok().build();
    }

    // group에 account가 있는지 조회
    @GetMapping("{groupId}")
    public LoanGroupAccountResponseDto getLoanGroupAccount(
            @PathVariable Integer groupId
    ){
        return loanGroupAccountService.getAccount(groupId);
    }

}
