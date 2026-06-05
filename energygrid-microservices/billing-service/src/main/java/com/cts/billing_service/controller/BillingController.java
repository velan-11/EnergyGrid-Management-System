package com.cts.billing_service.controller;


import com.cts.billing_service.dto.RequestDTO
        .BillingRequestDTO;
import com.cts.billing_service.dto.ResponseDTO
        .BillingResponseDTO;
import com.cts.billing_service.exception
        .BadRequestException;
import com.cts.billing_service.service
        .BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost
        .PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for legacy customer billing records.
 * Validates the request before delegating to the billing service.
 */
@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BillingResponseDTO>
    createBilling(
            @Valid @RequestBody
            BillingRequestDTO dto) {

        if (dto.getCustomerId() <= 0) {
            throw new BadRequestException(
                    "Invalid customer ID");
        }

        return ResponseEntity.ok(
                service.createBilling(dto));
    }

    @GetMapping("/{customerId}")
    @PreAuthorize(
            "hasAnyRole('ADMIN'," +
                    "'CUSTOMER','AUDITOR')")
    public ResponseEntity<
            List<BillingResponseDTO>>
    getBilling(
            @PathVariable Long customerId) {

        if (customerId <= 0) {
            throw new BadRequestException(
                    "Invalid customer ID");
        }

        List<BillingResponseDTO> bills =
                service.getBillingByCustomer(
                        customerId);

        if (bills.isEmpty()) {
            throw new BadRequestException(
                    "No billing records found " +
                            "for customer: " + customerId);
        }

        return ResponseEntity.ok(bills);
    }
}