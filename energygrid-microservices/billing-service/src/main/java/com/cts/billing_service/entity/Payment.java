package com.cts.billing_service.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The owning Invoice. The FK column is `invoice_id`; we expose the raw id
     * separately via getInvoiceId() so JSON responses include it without
     * forcing the consumer to navigate the relation graph.
     */
    @ManyToOne
    @JoinColumn(name = "invoice_id")
    @JsonBackReference
    private Invoice invoice;

    /** Customer the payment belongs to. Cached from invoice.customerId. */
    private Long customerId;

    private Double amount;
    private String paymentMethod;
    @Column(length = 64)
    private String transactionId;

    private LocalDateTime paymentDate;
    private String status;

    /** Convenience accessor — surfaces the FK in JSON without re-mapping it. */
    @Transient
    public Long getInvoiceId() {
        return invoice != null ? invoice.getId() : null;
    }

    @PrePersist
    public void onPersist() {
        if (this.paymentDate == null) this.paymentDate = LocalDateTime.now();
        if (this.status == null) this.status = "SUCCESS";
    }
}
