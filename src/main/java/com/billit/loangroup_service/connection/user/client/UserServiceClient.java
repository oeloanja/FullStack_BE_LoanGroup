package com.billit.loangroup_service.connection.user.client;

import com.billit.loangroup_service.config.FeignConfig;
import com.billit.loangroup_service.connection.user.dto.UserRequestDto;
import com.billit.loangroup_service.connection.user.dto.UserResponseDto;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "USER-SERVICE",
        configuration = FeignConfig.class,
        path = "/api/v1/user-service", url = "${feign.client.config.user-service.url}")
public interface UserServiceClient {
    @PostMapping("/accounts/transaction/group/borrow/deposit")
    List<UserResponseDto> requestDisbursement(@RequestBody List<UserRequestDto> requests);
}