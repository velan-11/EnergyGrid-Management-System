package com.cts.billing_service.dto.ResponseDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentResponseDTO {

    private Long id;
    private Double amount;
    private String paymentMethod;
    private LocalDateTime paymentDate;
    private String status;
}