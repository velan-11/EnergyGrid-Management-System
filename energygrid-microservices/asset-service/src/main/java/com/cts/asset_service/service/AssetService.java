package com.cts.asset_service.service;


import com.cts.asset_service.entity.Asset;
import com.cts.asset_service.repository.AssetRepository;
import com.cts.asset_service.dto.RequestDTO.AssetRequest;
import com.cts.asset_service.exception.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssetService {

    private final AssetRepository assetRepository;
    private final AuditService audit;

    public AssetService(AssetRepository assetRepository, AuditService audit) {
        this.assetRepository = assetRepository;
        this.audit = audit;
    }

    public Asset createAsset(AssetRequest req) {
        Asset asset = new Asset();
        asset.setName(req.getName());
        asset.setType(req.getType());
        asset.setLocation(req.getLocation());
        asset.setCapacityKW(req.getCapacityKW());
        asset.setCommissionedAt(req.getCommissionedAt());
        asset.setStatus(req.getStatus());
        Asset saved = assetRepository.save(asset);
        audit.log(null, null, "CREATE", "Asset", saved.getAssetId(),
                "name=" + saved.getName() + ", type=" + saved.getType()
                        + ", location=" + saved.getLocation()
                        + ", capacityKW=" + saved.getCapacityKW());
        return saved;
    }

    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }


    // UPDATE
    public Asset updateAsset(Long assetId, AssetRequest req) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + assetId));

        asset.setName(req.getName());
        asset.setType(req.getType());
        asset.setLocation(req.getLocation());
        asset.setCapacityKW(req.getCapacityKW());
        asset.setCommissionedAt(req.getCommissionedAt());
        asset.setStatus(req.getStatus());

        Asset saved = assetRepository.save(asset);
        audit.log(null, null, "UPDATE", "Asset", saved.getAssetId(),
                "name=" + saved.getName() + ", status=" + saved.getStatus()
                        + ", capacity=" + saved.getCapacityKW());
        return saved;
    }

    public Asset getAssetById(Long assetId) {
        return assetRepository.findById(assetId)
                .orElseThrow(() -> new EntityNotFoundException("Asset not found"));
    }



    // DELETE
    public void deleteAsset(Long assetId) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset not found: " + assetId);
        }
        assetRepository.deleteById(assetId);
        audit.log(null, null, "DELETE", "Asset", assetId, null);
    }


}

