package com.cts.scheduling_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Posts a notification to the notification-service. Each producer service
 * has a tiny copy of this client; the DTO is sent as a plain Map so no
 * shared types need to be published.
 */
@FeignClient(
        name = "notification-service-client",
        url = "http://localhost:8088"
)
public interface NotificationClient {

    @PostMapping("/api/notifications/create")
    void create(@RequestBody Map<String, Object> body);
}
