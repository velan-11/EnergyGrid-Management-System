package com.cts.billing_service.dto.RequestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class InvoiceRequestDTO {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private LocalDate periodStart;
    private LocalDate periodEnd;

    private LocalDate dueDate;

    /** Tax rate as a fraction (0.18 = 18%). Optional; defaults to 0. */
    @PositiveOrZero
    private Double taxRate;

    /** Optional explicit line items. If empty, energyUsed × unitPrice is used. */
    @Valid
    private List<LineItemDTO> lineItems;

    // ---- Legacy single-line fields (still supported) ----
    @PositiveOrZero
    private Double energyUsed;

    @PositiveOrZero
    private Double unitPrice;
}
