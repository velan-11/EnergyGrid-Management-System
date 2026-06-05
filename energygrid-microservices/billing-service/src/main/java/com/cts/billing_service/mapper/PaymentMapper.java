package com.cts.billing_service.mapper;

import com.cts.billing_service.dto.RequestDTO.PaymentRequestDTO;
import com.cts.billing_service.entity.Invoice;
import com.cts.billing_service.entity.Payment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/** Builds a Payment entity from a request DTO and its parent Invoice. */
@Component
public class PaymentMapper {

    public Payment toEntity(PaymentRequestDTO dto, Invoice invoice) {
        Payment payment = new Payment();
        // setInvoice() also makes getInvoiceId() return invoice.getId()
        // via the @Transient accessor on Payment.
        payment.setInvoice(invoice);
        payment.setCustomerId(invoice.getCustomerId());
        payment.setAmount(dto.getAmount());
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 14).toUpperCase());
        payment.setStatus("SUCCESS");
        return payment;
    }
}
