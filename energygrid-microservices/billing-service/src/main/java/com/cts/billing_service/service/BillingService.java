package com.cts.billing_service.service;


import com.cts.billing_service.dto.RequestDTO.BillingRequestDTO;
import com.cts.billing_service.dto.ResponseDTO.BillingResponseDTO;

import java.util.List;

public interface BillingService {
    BillingResponseDTO createBilling(BillingRequestDTO dto);
    List<BillingResponseDTO> getBillingByCustomer(Long customerId);
}
