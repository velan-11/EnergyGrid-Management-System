package com.cts.scheduling_service.dto.RequestDTO;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AssetDTO {
    private Long assetId;
    private Long ownerId;
    private String type;
    private String location;
    private BigDecimal capacityKW;
    private String status;
}