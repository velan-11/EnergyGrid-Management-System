package com.cts.billing_service.repository;

import com.cts.billing_service.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Derived from the Invoice relation — Spring Data resolves
     * `findByInvoice_Id` to `WHERE payment.invoice_id = ?`.
     */
    List<Payment> findByInvoice_Id(Long invoiceId);

    /** Original name kept as a default-method alias for older callers. */
    default List<Payment> findByInvoiceId(Long invoiceId) {
        return findByInvoice_Id(invoiceId);
    }

    List<Payment> findByCustomerIdOrderByPaymentDateDesc(Long customerId);

    List<Payment> findAllByOrderByPaymentDateDesc();
}
