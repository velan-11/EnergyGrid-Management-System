package com.cts.asset_service.service;

import com.cts.asset_service.entity.TelemetryPoint;
import com.cts.asset_service.repository.TelemetryPointRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/** Read-only access to an asset's telemetry history, ordered oldest-to-newest. */
@Service
public class TelemetryService {

    private final TelemetryPointRepository telemetryRepo;

    public TelemetryService(TelemetryPointRepository telemetryRepo) {
        this.telemetryRepo = telemetryRepo;
    }

    public List<TelemetryPoint> getTelemetryHistory(Long assetId) {
        return telemetryRepo.findByAssetIdOrderByTimestampAsc(assetId);
    }
}

