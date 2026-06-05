package com.cts.billing_service.service;

import com.cts.billing_service.dto.RequestDTO.InvoiceRequestDTO;
import com.cts.billing_service.dto.RequestDTO.LineItemDTO;
import com.cts.billing_service.entity.Invoice;
import com.cts.billing_service.entity.InvoiceLineItem;
import com.cts.billing_service.exception.ResourceNotFoundException;
import com.cts.billing_service.notification.NotificationPublisher;
import com.cts.billing_service.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class InvoiceService {

    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private NotificationPublisher notifications;
    @Autowired private AuditService audit;

    @Transactional
    public Invoice createInvoice(InvoiceRequestDTO dto) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(generateNumber());
        invoice.setCustomerId(dto.getCustomerId());
        invoice.setPeriodStart(dto.getPeriodStart());
        invoice.setPeriodEnd(dto.getPeriodEnd());
        invoice.setDueDate(dto.getDueDate() != null
                ? dto.getDueDate()
                : LocalDate.now().plusDays(15));

        // Resolve line items: explicit > legacy (energyUsed × unitPrice).
        List<InvoiceLineItem> items = new ArrayList<>();
        if (dto.getLineItems() != null && !dto.getLineItems().isEmpty()) {
            for (LineItemDTO l : dto.getLineItems()) {
                InvoiceLineItem it = new InvoiceLineItem();
                it.setDescription(l.getDescription());
                it.setQuantity(l.getQuantity());
                it.setUnitRate(l.getUnitRate());
                it.setAmount(round(l.getQuantity() * l.getUnitRate()));
                items.add(it);
            }
        } else if (dto.getEnergyUsed() != null && dto.getUnitPrice() != null
                && dto.getEnergyUsed() > 0 && dto.getUnitPrice() > 0) {
            InvoiceLineItem it = new InvoiceLineItem();
            it.setDescription("Energy consumption");
            it.setQuantity(dto.getEnergyUsed());
            it.setUnitRate(dto.getUnitPrice());
            it.setAmount(round(dto.getEnergyUsed() * dto.getUnitPrice()));
            items.add(it);
        }

        double subtotal = items.stream()
                .mapToDouble(i -> i.getAmount() == null ? 0.0 : i.getAmount())
                .sum();
        double taxRate = dto.getTaxRate() == null ? 0.0 : dto.getTaxRate();
        double tax = round(subtotal * taxRate);
        double total = round(subtotal + tax);

        invoice.setSubtotal(round(subtotal));
        invoice.setTax(tax);
        invoice.setAmount(total);
        invoice.setStatus("SENT");
        invoice.setCreatedAt(LocalDateTime.now());
        for (InvoiceLineItem it : items) invoice.addLineItem(it);

        Invoice saved = invoiceRepository.save(invoice);

        notifications.publish(
                saved.getCustomerId(),
                saved.getId(),
                "invoice",
                "BILLING",
                "INFO",
                "MEDIUM",
                "Invoice " + saved.getInvoiceNumber() + " is ready",
                "Total " + saved.getAmount() + " — due " + saved.getDueDate()
        );

        audit.log(null, null, "CREATE", "Invoice", saved.getId(),
                "number=" + saved.getInvoiceNumber()
                        + ", customer=" + saved.getCustomerId()
                        + ", total=" + saved.getAmount());

        return saved;
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public List<Invoice> getInvoicesForCustomer(Long customerId) {
        return invoiceRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
    }

    @Transactional
    public Invoice updateStatus(Long id, String status) {
        Invoice inv = getInvoiceById(id);
        inv.setStatus(status == null ? "DRAFT" : status.toUpperCase());
        if ("PAID".equals(inv.getStatus()) && inv.getPaidAt() == null) {
            inv.setPaidAt(LocalDateTime.now());
        }
        Invoice saved = invoiceRepository.save(inv);
        notifications.publish(
                saved.getCustomerId(),
                saved.getId(),
                "invoice",
                "BILLING",
                "PAID".equals(saved.getStatus()) ? "SUCCESS" : "INFO",
                "LOW",
                "Invoice " + saved.getInvoiceNumber() + " — " + saved.getStatus(),
                "Status updated to " + saved.getStatus()
        );
        audit.log(null, null, "STATUS_CHANGE", "Invoice", saved.getId(),
                "status=" + saved.getStatus());
        return saved;
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static String generateNumber() {
        return "INV-" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 10).toUpperCase();
    }
}
