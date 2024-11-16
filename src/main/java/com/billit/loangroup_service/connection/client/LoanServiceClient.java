package com.billit.loangroup_service.connection.client;

import com.billit.loangroup_service.connection.dto.LoanResponseClientDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "loan-service", url = "http://localhost:8083")
public interface LoanServiceClient {
    @GetMapping("/api/v1/loans/detail/{loanId}")
    LoanResponseClientDto getLoanById(@PathVariable Integer loanId);

    @PutMapping("/api/v1/loans/{loanId}/assign-group")
    void updateLoanGroup(@PathVariable Integer loanId, @RequestParam Integer groupId);

    @GetMapping("/api/v1/loans/list/{groupId}")
    List<LoanResponseClientDto> getLoansByGroupId(@PathVariable Integer groupId);

    @GetMapping("/api/loans/group/{groupId}/average-rate")
    Double getAverageInterestRateByGroupId(@PathVariable("groupId") Integer groupId);
}
