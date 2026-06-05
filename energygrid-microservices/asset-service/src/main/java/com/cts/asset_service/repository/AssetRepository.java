package com.cts.asset_service.repository;

import com.cts.asset_service.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRepository extends JpaRepository<Asset, Long> {
}

