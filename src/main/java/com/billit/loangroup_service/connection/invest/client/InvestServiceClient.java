package com.billit.loangroup_service.connection.invest.client;

import com.billit.loangroup_service.connection.invest.dto.InvestmentRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "invest-service", url = "http://localhost:8081")
public interface InvestServiceClient {
    @PutMapping("/api/v1/investments/updateBalance")
    void requestInvest(@RequestBody InvestmentRequestDto request);

    @PutMapping("/api/investments/group/{groupId}/investmentDate")
    void updateInvestmentDatesByGroupId(@PathVariable Integer groupId);
}
