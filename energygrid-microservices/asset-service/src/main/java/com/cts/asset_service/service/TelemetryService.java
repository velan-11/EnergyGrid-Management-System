package com.cts.asset_service.service;

import com.cts.asset_service.entity.TelemetryPoint;
import com.cts.asset_service.repository.TelemetryPointRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

