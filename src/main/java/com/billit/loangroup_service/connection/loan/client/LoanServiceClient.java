package com.billit.loangroup_service.connection.loan.client;

import com.billit.loangroup_service.connection.loan.dto.LoanResponseClientDto;
import com.billit.loangroup_service.connection.loan.dto.LoanStatusUpdateRequestDto;
import com.billit.loangroup_service.connection.loan.dto.LoanSuccessStatusRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "LOAN-SERVICE", url = "${feign.client.config.loan-service.url}")
public interface LoanServiceClient {
    @GetMapping("/api/v1/loan-service/detail/{loanId}")
    LoanResponseClientDto getLoanById(@PathVariable Integer loanId);

    @GetMapping("/api/v1/loan-service/list/{groupId}")
    List<LoanResponseClientDto> getLoansByGroupId(@PathVariable Integer groupId);
}