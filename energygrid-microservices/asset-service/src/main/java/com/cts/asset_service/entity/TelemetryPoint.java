package com.cts.asset_service.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TelemetryPoint")
public class TelemetryPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TelemetryID")
    private Long telemetryId;

    @Column(name = "AssetID", nullable = false)
    private Long assetId;

    @Column(name = "Timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "Metric", nullable = false)
    private String metric;

    @Column(name = "Value", nullable = false)
    private Double value;

    @Column(name = "IngestedAt", nullable = false)
    private LocalDateTime ingestedAt;

    @Column(name = "Source")
    private String source;

    @JsonProperty("TelemetryID")
    public Long getTelemetryId() { return telemetryId; }
    public void setTelemetryId(Long telemetryId) { this.telemetryId = telemetryId; }

    @JsonProperty("AssetID")
    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    @JsonProperty("Timestamp")
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @JsonProperty("Metric")
    public String getMetric() { return metric; }
    public void setMetric(String metric) { this.metric = metric; }

    @JsonProperty("Value")
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    @JsonProperty("IngestedAt")
    public LocalDateTime getIngestedAt() { return ingestedAt; }
    public void setIngestedAt(LocalDateTime ingestedAt) { this.ingestedAt = ingestedAt; }

    @JsonProperty("Source")
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
