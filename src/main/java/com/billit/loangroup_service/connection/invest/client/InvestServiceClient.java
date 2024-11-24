package com.billit.loangroup_service.connection.invest.client;

import com.billit.loangroup_service.connection.invest.dto.InvestmentRequestDto;
import com.billit.loangroup_service.connection.invest.dto.SettlementRatioRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "invest-service", url = "http://localhost:8081")
public interface InvestServiceClient {
    // 차액 반환 요청 api < 수정 필요하다 함
    @PutMapping("/api/v1/invest-service/investments/updateBalance")
    void refundUpdateInvestAmount(@RequestBody InvestmentRequestDto request);

    // 투자 실행일 업데이트 요청 api <
    @PutMapping("/api/v1/invest-service/investments/group/{groupId}/updateInvestmentDate")
    void updateInvestmentDatesByGroupId(@PathVariable Integer groupId);

    // 투자 정산 비율 설정 api
    @PutMapping("/api/v1/invest-service/investments/group/updateSettlementRatio")
    void updateSettlementRatioByGroupId(@RequestBody SettlementRatioRequestDto request);
}
