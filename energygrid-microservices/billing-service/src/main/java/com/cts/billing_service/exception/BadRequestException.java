package com.cts.billing_service.exception;

public class BadRequestException
        extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}