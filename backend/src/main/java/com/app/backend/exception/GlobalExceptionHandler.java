package com.app.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return handleNotFound(ex, null);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        logClientError(HttpStatus.NOT_FOUND, request, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorBody("NOT_FOUND", ex.getMessage()));
    }

    public ResponseEntity<Map<String, Object>> handleInvalidToken(InvalidTokenException ex) {
        return handleInvalidToken(ex, null);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidToken(
            InvalidTokenException ex,
            HttpServletRequest request) {
        logClientError(HttpStatus.UNAUTHORIZED, request, ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorBody("UNAUTHORIZED", "Authentication failed"));
    }

    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return handleAccessDenied(ex, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {
        logClientError(HttpStatus.FORBIDDEN, request, "Access denied");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(errorBody("FORBIDDEN", "Access denied"));
    }

    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        return handleValidation(ex, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        List<Map<String, String>> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "reason", error.getDefaultMessage() != null ? error.getDefaultMessage() : "invalid"))
                .toList();

        String fields = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField())
                .distinct()
                .sorted()
                .reduce((left, right) -> left + "," + right)
                .orElse("unknown");

        logClientError(HttpStatus.BAD_REQUEST, request, "Request validation failed for fields=" + fields);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "VALIDATION_FAILED",
                        "message", "Request validation failed",
                        "details", details,
                        "timestamp", Instant.now().toString()));
    }

    public ResponseEntity<Map<String, Object>> handleNoResource(NoResourceFoundException ex) {
        return handleNoResource(ex, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResource(
            NoResourceFoundException ex,
            HttpServletRequest request) {
        logClientError(HttpStatus.NOT_FOUND, request, "Resource not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorBody("NOT_FOUND", "Resource not found"));
    }

    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return handleTypeMismatch(ex, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        String message = "Invalid parameter value for " + ex.getName();
        logClientError(HttpStatus.BAD_REQUEST, request, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorBody("BAD_REQUEST", "Invalid parameter value"));
    }

    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        return handleUnreadable(ex, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        logClientError(HttpStatus.BAD_REQUEST, request, "Malformed request body");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorBody("BAD_REQUEST", "Malformed request body"));
    }

    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        return handleMissingParam(ex, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        String message = "Required parameter '" + ex.getParameterName() + "' is missing";
        logClientError(HttpStatus.BAD_REQUEST, request, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorBody("BAD_REQUEST", message));
    }

    public ResponseEntity<Map<String, Object>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return handleMethodNotAllowed(ex, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        logClientError(HttpStatus.METHOD_NOT_ALLOWED, request, ex.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(errorBody("METHOD_NOT_ALLOWED", ex.getMessage()));
    }

    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return handleGeneric(ex, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(
            Exception ex,
            HttpServletRequest request) {
        log.error("Unhandled exception status={} method={} path={}",
                HttpStatus.INTERNAL_SERVER_ERROR.value(), requestMethod(request), requestPath(request), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody("INTERNAL_ERROR", "An unexpected error occurred"));
    }

    private void logClientError(HttpStatus status, HttpServletRequest request, String message) {
        log.warn("Handled exception status={} method={} path={} message={}",
                status.value(), requestMethod(request), requestPath(request), message);
    }

    private String requestMethod(HttpServletRequest request) {
        return request != null ? request.getMethod() : "N/A";
    }

    private String requestPath(HttpServletRequest request) {
        return request != null ? request.getRequestURI() : "N/A";
    }

    private Map<String, Object> errorBody(String error, String message) {
        return Map.of(
                "error", error,
                "message", message,
                "timestamp", Instant.now().toString()
        );
    }
}
