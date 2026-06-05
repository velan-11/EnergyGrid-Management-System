package com.cts.asset_service.controller;
import com.cts.asset_service.dto.RequestDTO.AssetRequest;
import com.cts.asset_service.entity.Asset;
import com.cts.asset_service.service.AssetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

/** REST endpoints for managing assets; access is gated per-method by role via @PreAuthorize. */
@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','PRODUCER')")
    public ResponseEntity<Asset> createAsset(@Valid @RequestBody AssetRequest request) {
        Asset saved = assetService.createAsset(request);
        return ResponseEntity.created(URI.create("/api/assets/" + saved.getAssetId())
        ).body(saved);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','TECHNICIAN','PRODUCER','AUDITOR')")
    public List<Asset> getAssets() {
        return assetService.getAllAssets();
    }

    @PutMapping("/put/{assetId}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','PRODUCER')")
    public ResponseEntity<Asset> updateAsset(
            @PathVariable Long assetId,
            @Valid @RequestBody AssetRequest request) {
        Asset updated = assetService.updateAsset(assetId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{assetId}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long assetId) {
        assetService.deleteAsset(assetId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{assetId}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','TECHNICIAN','PRODUCER','AUDITOR')")
    public ResponseEntity<Asset> getAssetById(@PathVariable Long assetId) {
        Asset asset = assetService.getAssetById(assetId);
        return ResponseEntity.ok(asset);
    }
}

