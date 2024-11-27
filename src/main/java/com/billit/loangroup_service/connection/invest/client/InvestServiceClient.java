package com.billit.loangroup_service.connection.invest.client;

import com.billit.loangroup_service.connection.invest.dto.RefundRequestDto;
import com.billit.loangroup_service.connection.invest.dto.SettlementRatioRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "INVESTMENT-SERVICE", url = "${feign.client.config.invest-service.url}")
public interface InvestServiceClient {
    // 차액 반환 요청 api < 수정 필요하다 함
    @PutMapping("/api/v1/invest-service/investments/updateBalance")
    void refundUpdateInvestAmount(@RequestBody RefundRequestDto request);

    // 투자 실행일 업데이트 요청 api <
    @PutMapping("/api/v1/invest-service/investments/group/{groupId}/updateInvestmentDate")
    ResponseEntity<String> updateInvestmentDatesByGroupId(@PathVariable Integer groupId);

    // 투자 정산 비율 설정 api
    @PutMapping("/api/v1/invest-service/investments/group/updateSettlementRatio")
    void updateSettlementRatioByGroupId(@RequestBody SettlementRatioRequestDto request);
}
