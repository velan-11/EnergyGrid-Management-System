package com.cts.billing_service.dto.RequestDTO;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PaymentRequestDTO {

    @NotNull(message = "Invoice ID is required")
    private Long invoiceId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private Double amount;

    @NotBlank(message = "Payment method is required")
    @Size(max = 50, message = "Payment method too long")
    private String paymentMethod;
}