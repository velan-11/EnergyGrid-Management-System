package com.cts.scheduling_service.dto.ResponseDTO;

import java.time.LocalDateTime;

public class ErrorResponseDTO {
    private int status;
    private String message;
    private String errorCode;
    private String path;
    private LocalDateTime timestamp;
    private String details;

    public ErrorResponseDTO(int status, String message, String errorCode) {
        this.status = status;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponseDTO(int status, String message, String errorCode, String path) {
        this(status, message, errorCode);
        this.path = path;
    }

    public ErrorResponseDTO(int status, String message, String errorCode, String path, String details) {
        this(status, message, errorCode, path);
        this.details = details;
    }

    // Getters and Setters
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}

