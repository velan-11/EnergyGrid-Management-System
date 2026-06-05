package com.cts.billing_service.client;

import com.cts.billing_service.dto.RequestDTO.AssetDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation
        .GetMapping;
import org.springframework.web.bind.annotation
        .PathVariable;

@FeignClient(
        name = "asset-service",
        url = "http://localhost:8082"
)
public interface AssetServiceClient {

    @GetMapping("/api/assets/{assetId}")
    AssetDTO getAssetById(
            @PathVariable Long assetId);
}