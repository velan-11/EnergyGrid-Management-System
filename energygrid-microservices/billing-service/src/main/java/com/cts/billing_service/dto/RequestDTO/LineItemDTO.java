package com.cts.billing_service.dto.RequestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class LineItemDTO {

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull
    @Positive(message = "Quantity must be > 0")
    private Double quantity;

    @NotNull
    @Positive(message = "Unit rate must be > 0")
    private Double unitRate;
}
