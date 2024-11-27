package com.billit.loangroup_service.connection.repayment.client;

import com.billit.loangroup_service.connection.repayment.dto.RepaymentRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "REPAYMENT-SERVICE", url = "${feign.client.config.repayment-service.url}")
public interface RepaymentClient {
    @PostMapping("/api/v1/repayment-service/create")
    void createRepayment(@RequestBody RepaymentRequestDto request);
}
