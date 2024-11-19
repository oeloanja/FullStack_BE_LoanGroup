package com.billit.loangroup_service.connection.user.client;

import com.billit.loangroup_service.connection.user.dto.UserRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserServiceClient {
    @PostMapping("/api/v1/users/disbursement")
    boolean requestDisbursement(@RequestBody List<UserRequestDto> requests);
}
