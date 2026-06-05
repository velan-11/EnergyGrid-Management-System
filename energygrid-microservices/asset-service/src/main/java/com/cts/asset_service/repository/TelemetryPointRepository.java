package com.cts.asset_service.repository;

import com.cts.asset_service.entity.TelemetryPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TelemetryPointRepository
        extends JpaRepository<TelemetryPoint, Long> {

    List<TelemetryPoint> findByAssetIdOrderByTimestampAsc(Long assetId);
}

