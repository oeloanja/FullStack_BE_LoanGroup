package com.billit.loangroup_service.controller;

import com.billit.loangroup_service.dto.LoanGroupAccountRequestDto;
import com.billit.loangroup_service.dto.LoanGroupAccountResponseDto;
import com.billit.loangroup_service.exception.LoanGroupException;
import com.billit.loangroup_service.service.LoanGroupAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/v1/loan-group-service/account")
public class LoanGroupAccountController {
    private final LoanGroupAccountService loanGroupAccountService;

    // 투자금 현황 업데이트
    @PutMapping("/invest")
    public ResponseEntity<String> updatePlatformAccountBalance(
            @Valid @RequestBody LoanGroupAccountRequestDto request) {
        try {
            log.info("Received request: {}", request);  // 요청 로그 추가
            loanGroupAccountService.updateLoanGroupAccountBalance(request);
            return ResponseEntity.ok("투자금이 성공적으로 입금되었습니다.");
        } catch (LoanGroupException e) {
            log.error("Failed to process investment: {}", e.getMessage());  // 에러 로그 추가
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error: ", e);  // 예상치 못한 에러 로그 추가
            return ResponseEntity.badRequest().body("처리 중 오류가 발생했습니다.");
        }
    }

    // group에 account가 있는지 조회
    @GetMapping("{groupId}")
    public LoanGroupAccountResponseDto getLoanGroupAccount(
            @PathVariable Integer groupId
    ){
        return loanGroupAccountService.getAccount(groupId);
    }

}
