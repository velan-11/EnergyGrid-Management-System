package com.cts.scheduling_service.exception;

public class InvalidDataException extends RuntimeException {
    private String field;
    private String reason;

    public InvalidDataException(String message) {
        super(message);
    }

    public InvalidDataException(String field, String reason) {
        super("Invalid data for field: " + field + ". Reason: " + reason);
        this.field = field;
        this.reason = reason;
    }

    public String getField() {
        return field;
    }

    public String getReason() {
        return reason;
    }
}

