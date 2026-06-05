package com.cts.billing_service.mapper;

import com.cts.billing_service.dto.RequestDTO.BillingRequestDTO;
import com.cts.billing_service.dto.ResponseDTO.BillingResponseDTO;
import com.cts.billing_service.entity.Billing;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class BillingMapper {

    public Billing toEntity(BillingRequestDTO dto) {
        Billing billing = new Billing();

        // ✅ IMPORTANT: do NOT touch ID
        billing.setCustomerId(dto.getCustomerId());
        billing.setAmount(dto.getAmount());
        billing.setDueDate(dto.getDueDate());

        return billing;
    }

    public BillingResponseDTO toDTO(Billing billing) {
        BillingResponseDTO dto = new BillingResponseDTO();
        dto.setId(billing.getId());
        dto.setCustomerId(billing.getCustomerId());
        dto.setAmount(billing.getAmount());
        dto.setStatus(billing.getStatus());
        dto.setDueDate(billing.getDueDate());
        return dto;
    }
}