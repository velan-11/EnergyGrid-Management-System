package com.cts.billing_service.exception;

public class BillingNotFoundException
        extends RuntimeException {
    public BillingNotFoundException(Long id) {
        super("Billing not found with id: " + id);
    }
}