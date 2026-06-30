package com.smartdealhub.smartdealhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        return build(HttpStatus.BAD_REQUEST, "Validation failed");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage() == null ? "Validation failed" : ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        String message = ex.getMessage() == null ? "Unexpected error" : ex.getMessage();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (message.toLowerCase().contains("not found")) status = HttpStatus.NOT_FOUND;
        else if (message.toLowerCase().contains("invalid credentials")) status = HttpStatus.UNAUTHORIZED;
        else if (message.toLowerCase().contains("invalid")) status = HttpStatus.BAD_REQUEST;
        else if (message.toLowerCase().contains("forbidden") || message.toLowerCase().contains("pending")) status = HttpStatus.FORBIDDEN;
        else if (message.toLowerCase().contains("deactivated") || message.toLowerCase().contains("inactive")) status = HttpStatus.FORBIDDEN;
        else if (message.toLowerCase().contains("unauthenticated")) status = HttpStatus.UNAUTHORIZED;
        else if (message.toLowerCase().contains("already")) status = HttpStatus.CONFLICT;
        return build(status, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAny(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
