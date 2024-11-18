package com.billit.loangroup_service.connection.client;

import com.billit.loangroup_service.connection.dto.InvestmentRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "invest-service", url = "http://localhost:8081")
public interface InvestServiceClient {
    @PutMapping("/api/v1/investments/updateBalance")
    void requestInvest(@RequestBody InvestmentRequestDto request);
}
