package com.cts.outage_service.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * One place where every uncaught exception from the outage-service ends up.
 * Returns a consistent `{ error, message, status }` JSON body so the frontend
 * can surface a real error message instead of a blank 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        return error(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "VALIDATION_FAILED");
        body.put("status", HttpStatus.BAD_REQUEST.value());
        ex.getBindingResult().getFieldErrors().forEach(
                f -> body.put(f.getField(), f.getDefaultMessage()));
        body.put("message", "One or more fields are invalid");
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(AccessDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, "FORBIDDEN",
                "You don't have permission to perform this action.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        // Surfaces FK violations etc. with a friendlier message than the SQL trace.
        Throwable root = ex.getMostSpecificCause();
        String msg = root != null && root.getMessage() != null
                ? "Could not complete operation: " + summarise(root.getMessage())
                : "Could not complete operation due to data constraints.";
        return error(HttpStatus.CONFLICT, "DATA_CONFLICT", msg);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR",
                ex.getMessage() == null ? "Unexpected error" : ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR",
                ex.getMessage() == null ? "Unexpected error" : ex.getMessage());
    }

    private static ResponseEntity<Map<String, Object>> error(HttpStatus status, String code, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", code);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    /** Truncate the DB error so we don't leak a multi-line SQL trace. */
    private static String summarise(String s) {
        int newline = s.indexOf('\n');
        String first = newline > 0 ? s.substring(0, newline) : s;
        return first.length() > 200 ? first.substring(0, 200) + "..." : first;
    }
}
