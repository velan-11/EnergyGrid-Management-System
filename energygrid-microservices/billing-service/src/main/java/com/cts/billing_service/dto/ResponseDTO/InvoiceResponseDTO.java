package com.cts.billing_service.dto.ResponseDTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InvoiceResponseDTO {

    private Long id;
    private Long customerId;
    private Double amount;
    private String status;
    private LocalDateTime createdAt;
}