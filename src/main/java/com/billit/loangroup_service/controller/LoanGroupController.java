package com.billit.loangroup_service.controller;

import com.billit.loangroup_service.connection.dto.LoanRequestClientDto;
import com.billit.loangroup_service.dto.LoanGroupResponseDto;
import com.billit.loangroup_service.enums.RiskLevel;
import com.billit.loangroup_service.service.LoanGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/loans/group")
public class LoanGroupController {
    private final LoanGroupService loanGroupService;

    @PostMapping("/register")
    public LoanGroupResponseDto assignGroup(@RequestBody LoanRequestClientDto request) {
        if (request == null || request.getLoanId() == null) {
            throw new IllegalArgumentException("Invalid request: loanId must not be null");
        }
        return loanGroupService.assignGroup(request);
    }

//    @GetMapping("/list/{riskLevel}")
//    public List<LoanGroupResponseDto> getGroupList(@PathVariable RiskLevel riskLevel) {
//        return loanGroupService.getActiveGroupsWithLoanGroupAccount(riskLevel);
//    }
}