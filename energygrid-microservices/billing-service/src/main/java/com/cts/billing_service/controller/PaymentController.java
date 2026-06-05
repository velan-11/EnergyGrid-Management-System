package com.cts.billing_service.controller;

import com.cts.billing_service.dto.RequestDTO.PaymentRequestDTO;
import com.cts.billing_service.entity.Payment;
import com.cts.billing_service.exception.BadRequestException;
import com.cts.billing_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST endpoints for recording and querying invoice payments. */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<Payment> recordPayment(
            @Valid @RequestBody PaymentRequestDTO dto) {
        if (dto.getInvoiceId() == null || dto.getInvoiceId() <= 0) {
            throw new BadRequestException("Invalid invoice ID");
        }
        if (dto.getAmount() == null || dto.getAmount() <= 0) {
            throw new BadRequestException("Payment amount must be greater than 0");
        }
        return ResponseEntity.ok(service.recordPayment(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    public ResponseEntity<List<Payment>> all() {
        return ResponseEntity.ok(service.all());
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER','AUDITOR')")
    public ResponseEntity<List<Payment>> byCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(service.byCustomer(customerId));
    }

    @GetMapping("/invoice/{invoiceId}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER','AUDITOR')")
    public ResponseEntity<List<Payment>> byInvoice(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(service.byInvoice(invoiceId));
    }
}
