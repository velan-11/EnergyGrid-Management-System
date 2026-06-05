package com.cts.asset_service.entity;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/** A physical grid asset (Solar / Wind / Battery / Meter) persisted to the Asset table. */
@Entity
@Table(name = "Asset")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AssetID")
    private Long assetId;

    @Column(name = "Name", length = 120)
    private String name;

    @Column(name = "Type", nullable = false)
    private String type; // Solar / Wind / Battery / Meter

    @Column(name = "Location", nullable = false)
    private String location;


    @Column(name = "CapacityKW", precision = 10, scale = 2)
    private BigDecimal capacityKW;


    @Column(name = "CommissionedAt")
    private LocalDateTime commissionedAt;

    @Column(name = "Status", nullable = false)
    private String status;

    @JsonProperty("AssetID")
    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    @JsonProperty("Name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @JsonProperty("Type")
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @JsonProperty("Location")
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }


    @JsonProperty("CapacityKW")
    public BigDecimal getCapacityKW() {
        return capacityKW;
    }

    public void setCapacityKW(BigDecimal capacityKW) {
        this.capacityKW = capacityKW;
    }


    @JsonProperty("CommissionedAt")
    public LocalDateTime getCommissionedAt() { return commissionedAt; }
    public void setCommissionedAt(LocalDateTime commissionedAt) { this.commissionedAt = commissionedAt; }

    @JsonProperty("Status")
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
