package com.cts.billing_service.dto.ResponseDTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BillingResponseDTO {
    private Long id;
    private Long customerId;
    private Double amount;
    private LocalDateTime dueDate;
    private LocalDateTime generatedAt;
    private String status;
    private String billUri;
}