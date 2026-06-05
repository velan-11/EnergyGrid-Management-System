package com.cts.asset_service.repository;

import com.cts.asset_service.entity.TelemetryPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** Data access for telemetry points; derived queries fetch readings for a given asset. */
public interface TelemetryPointRepository
        extends JpaRepository<TelemetryPoint, Long> {

    // Chronological history for one asset, used to build trend/time-series views
    List<TelemetryPoint> findByAssetIdOrderByTimestampAsc(Long assetId);
}

