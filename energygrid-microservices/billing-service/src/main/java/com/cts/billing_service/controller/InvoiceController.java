package com.cts.billing_service.controller;

import com.cts.billing_service.dto.RequestDTO.InvoiceRequestDTO;
import com.cts.billing_service.entity.Invoice;
import com.cts.billing_service.exception.BadRequestException;
import com.cts.billing_service.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** REST endpoints for creating, listing and updating invoices. */
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Invoice> createInvoice(
            @Valid @RequestBody InvoiceRequestDTO dto) {
        if (dto.getCustomerId() == null || dto.getCustomerId() <= 0) {
            throw new BadRequestException("Invalid customer ID");
        }
        // Require at least one amount source: explicit line items OR the legacy pair.
        boolean hasLineItems = dto.getLineItems() != null && !dto.getLineItems().isEmpty();
        boolean hasLegacyAmount = dto.getEnergyUsed() != null && dto.getUnitPrice() != null
                && dto.getEnergyUsed() > 0 && dto.getUnitPrice() > 0;
        if (!hasLineItems && !hasLegacyAmount) {
            throw new BadRequestException(
                    "Provide either lineItems or energyUsed + unitPrice");
        }
        return ResponseEntity.ok(service.createInvoice(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    public ResponseEntity<List<Invoice>> list() {
        return ResponseEntity.ok(service.getAllInvoices());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER','AUDITOR')")
    public ResponseEntity<Invoice> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getInvoiceById(id));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER','AUDITOR')")
    public ResponseEntity<List<Invoice>> byCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(service.getInvoicesForCustomer(customerId));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Invoice> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.updateStatus(id, body.get("status")));
    }
}
