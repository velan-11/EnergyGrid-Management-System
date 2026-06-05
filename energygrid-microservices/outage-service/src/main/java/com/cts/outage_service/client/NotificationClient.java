package com.cts.outage_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/** Feign client that posts notifications as a generic key/value map body. */
@FeignClient(
        name = "notification-service-client",
        url = "http://localhost:8088"
)
public interface NotificationClient {

    @PostMapping("/api/notifications/create")
    void create(@RequestBody Map<String, Object> body);
}
