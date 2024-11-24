package com.billit.loangroup_service.connection.user.client;

import com.billit.loangroup_service.config.FeignConfig;
import com.billit.loangroup_service.connection.user.dto.UserRequestDto;
import com.billit.loangroup_service.connection.user.dto.UserResponseDto;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "user-service", url="localhost:8085", configuration = FeignConfig.class)
public interface UserServiceClient {
    @PostMapping("/api/v1/user-service/accounts/transaction/group/borrow/deposit")
    List<UserResponseDto> requestDisbursement(@RequestBody List<UserRequestDto> requests);
}