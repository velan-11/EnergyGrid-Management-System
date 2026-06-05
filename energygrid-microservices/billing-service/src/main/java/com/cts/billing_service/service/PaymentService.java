package com.cts.billing_service.service;

import com.cts.billing_service.dto.RequestDTO.PaymentRequestDTO;
import com.cts.billing_service.entity.Invoice;
import com.cts.billing_service.entity.Payment;
import com.cts.billing_service.exception.BadRequestException;
import com.cts.billing_service.exception.ResourceNotFoundException;
import com.cts.billing_service.mapper.PaymentMapper;
import com.cts.billing_service.notification.NotificationPublisher;
import com.cts.billing_service.repository.InvoiceRepository;
import com.cts.billing_service.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private PaymentMapper mapper;
    @Autowired private NotificationPublisher notifications;

    @Transactional
    public Payment recordPayment(PaymentRequestDTO dto) {
        Invoice invoice = invoiceRepository.findById(dto.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        if (isAlreadyPaid(invoice)) {
            throw new BadRequestException("Invoice is already paid. Cannot record another payment.");
        }

        Payment payment = mapper.toEntity(dto, invoice);
        invoice.getPayments().add(payment);

        double paidSoFar = sumExistingPayments(invoice) + dto.getAmount();
        double total = invoice.getAmount() == null ? 0.0 : invoice.getAmount();

        if (paidSoFar >= total) {
            invoice.setStatus("PAID");
            invoice.setPaidAt(LocalDateTime.now());
        } else {
            invoice.setStatus("PARTIAL");
        }

        Payment saved = paymentRepository.save(payment);

        notifications.publish(
                invoice.getCustomerId(),
                invoice.getId(),
                "invoice",
                "BILLING",
                "SUCCESS",
                "LOW",
                "Payment received on invoice " + invoice.getInvoiceNumber(),
                "Amount " + dto.getAmount() + " (" + payment.getPaymentMethod() + ")"
        );

        return saved;
    }

    private boolean isAlreadyPaid(Invoice invoice) {
        String status = invoice.getStatus();
        return status != null && status.equalsIgnoreCase("PAID");
    }

    private double sumExistingPayments(Invoice invoice) {
        return invoice.getPayments().stream()
                .filter(p -> p.getId() != null)
                .mapToDouble(p -> p.getAmount() == null ? 0.0 : p.getAmount())
                .sum();
    }

    public List<Payment> all() {
        return paymentRepository.findAllByOrderByPaymentDateDesc();
    }

    public List<Payment> byCustomer(Long customerId) {
        return paymentRepository.findByCustomerIdOrderByPaymentDateDesc(customerId);
    }

    public List<Payment> byInvoice(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId);
    }
}
