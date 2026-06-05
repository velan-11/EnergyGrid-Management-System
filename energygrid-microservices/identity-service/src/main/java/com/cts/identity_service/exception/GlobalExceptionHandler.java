package com.cts.identity_service.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EvidenceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEvidence(EvidenceNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "EVIDENCE_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(WorkOrderNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleWorkOrder(WorkOrderNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "WORK_ORDER_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(TechnicianNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTechnician(TechnicianNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "TECHNICIAN_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResource(ResourceNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        return error(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(DuplicateEmailException ex) {
        return error(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", ex.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidToken(InvalidTokenException ex) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "VALIDATION_FAILED");
        Map<String, String> fields = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> fields.put(e.getField(), e.getDefaultMessage()));
        body.putAll(fields);
        // Surface the first field-level message in `message` so the toast
        // shows something concrete by default.
        body.put("message", fields.values().stream().findFirst()
                .orElse("One or more fields are invalid"));
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        // Username / email unique constraints fall through here. We surface a
        // friendly message instead of leaking the SQL trace.
        Throwable root = ex.getMostSpecificCause();
        String raw = root != null && root.getMessage() != null ? root.getMessage() : "";
        String msg;
        String code = "DATA_CONFLICT";
        if (raw.toLowerCase().contains("username")) {
            msg = "That username is already taken. Pick another.";
            code = "DUPLICATE_USERNAME";
        } else if (raw.toLowerCase().contains("email")) {
            msg = "That email is already registered.";
            code = "DUPLICATE_EMAIL";
        } else {
            msg = "Could not complete operation due to data constraints.";
        }
        return error(HttpStatus.CONFLICT, code, msg);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        return error(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                ex.getMessage() == null ? "Bad request" : ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR",
                ex.getMessage() == null ? "Unexpected error" : ex.getMessage());
    }

    private static ResponseEntity<Map<String, Object>> error(
            HttpStatus status, String code, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", code);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
