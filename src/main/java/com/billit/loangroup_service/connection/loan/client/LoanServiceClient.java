package com.billit.loangroup_service.connection.loan.client;

import com.billit.loangroup_service.connection.loan.dto.LoanResponseClientDto;
import com.billit.loangroup_service.connection.loan.dto.LoanStatusUpdateRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "LOAN-SERVICE", url = "${feign.client.config.loan-service.url}")
public interface LoanServiceClient {
    @GetMapping("/api/v1/loan-service/detail/{loanId}")
    LoanResponseClientDto getLoanById(@PathVariable Integer loanId);

    // LoanService에서 loanId를 보내면 loanGroup을 update함
    @PutMapping("/api/v1/loan-service/{loanId}/assign-group")
    void updateLoanGroup(@PathVariable Integer loanId, @RequestParam Integer groupId);

    @GetMapping("/api/v1/loan-service/list/{groupId}")
    List<LoanResponseClientDto> getLoansByGroupId(@PathVariable Integer groupId);

    @GetMapping("/api/loan-service/group/{groupId}/average-rate")
    Double getAverageInterestRateByGroupId(@PathVariable("groupId") Integer groupId);

    @PutMapping("/api/v1/loan-service/status")
    void updateLoanStatus(@RequestBody LoanStatusUpdateRequestDto request);

    @GetMapping("/api/v1/loan-service/group/{groupId}")
    List<LoanResponseClientDto> getLoansByGroupId(@PathVariable String groupId);

    default void updateLoansStatus(List<LoanStatusUpdateRequestDto> requests) {
        requests.forEach(this::updateLoanStatus);
    }
}