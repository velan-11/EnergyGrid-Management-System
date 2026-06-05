package com.cts.asset_service.dto.RequestDTO;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class AssetRequest {

    @JsonProperty("Name")
    @Size(max = 120, message = "Name must be 120 characters or less")
    private String name;

    @JsonProperty("Type")
    @NotBlank(message = "Type is required")
    @Pattern(
            regexp = "Solar|Wind|Battery|Meter",
            message = "Type must be Solar, Wind, Battery, or Meter"
    )
    private String type;

    @JsonProperty("Location")
    @NotBlank(message = "Location cannot be empty")
    private String location;


    @JsonProperty("CapacityKW")
    @DecimalMin(value = "0.0", inclusive = false, message = "CapacityKW must be greater than 0")
    private BigDecimal capacityKW;


    @JsonProperty("CommissionedAt")
    private LocalDateTime commissionedAt;

    @JsonProperty("Status")
    @NotBlank(message = "Status is required")
    private String status;



    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }


    public BigDecimal getCapacityKW() {
        return capacityKW;
    }

    public void setCapacityKW(BigDecimal capacityKW) {
        this.capacityKW = capacityKW;
    }


    public LocalDateTime getCommissionedAt() { return commissionedAt; }
    public void setCommissionedAt(LocalDateTime commissionedAt) { this.commissionedAt = commissionedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
