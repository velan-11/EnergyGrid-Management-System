package com.cts.billing_service.client;

import com.cts.billing_service.dto.RequestDTO.UserDTO;
import org.springframework.cloud.openfeign
        .FeignClient;
import org.springframework.web.bind.annotation
        .GetMapping;
import org.springframework.web.bind.annotation
        .PathVariable;

@FeignClient(
        name = "identity-service",
        url = "http://localhost:8081"
)
public interface UserServiceClient {

    @GetMapping("/api/admin/users/{userId}")
    UserDTO getUserById(
            @PathVariable Long userId);
}