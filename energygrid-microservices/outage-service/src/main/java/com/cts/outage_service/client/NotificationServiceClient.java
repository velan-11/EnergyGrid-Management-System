package com.cts.outage_service.client;

import com.cts.outage_service.dto.RequestDTO.NotificationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "notification-service",
        url = "http://localhost:8088"
)
public interface NotificationServiceClient {

    @PostMapping("/api/notifications/create")
    void createNotification(
            @RequestBody NotificationDTO dto);
}