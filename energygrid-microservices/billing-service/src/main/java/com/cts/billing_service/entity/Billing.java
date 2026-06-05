package com.cts.billing_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "billing")
@Data
public class Billing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;

    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private LocalDateTime createdAt;
    private Double amount;

    private LocalDateTime generatedAt;
    private LocalDateTime dueDate;

    private String status;
    private String billUri;

    @PrePersist
    public void onCreate() {
        this.generatedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }
}