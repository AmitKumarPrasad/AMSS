package com.edusphere.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final Pattern SAFE_REQUEST_ID = Pattern.compile("[A-Za-z0-9._:-]{1,100}");

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        var response = build(ex.getStatusCode().value(), reason(ex, "Request failed"), request, List.of());
        if (ex.getStatusCode().value() == 429)
            return ResponseEntity.status(429).headers(response.getHeaders()).header("Retry-After", "60").body(response.getBody());
        return response;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ValidationError(error.getField(), safeMessage(error)))
                .toList();
        return build(HttpStatus.BAD_REQUEST.value(), "Validation failed", request, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<ValidationError> errors = ex.getConstraintViolations().stream()
                .map(error -> new ValidationError(error.getPropertyPath().toString(), error.getMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST.value(), "Validation failed", request, errors);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN.value(), "Access denied", request, List.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED.value(), "Invalid credentials", request, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT.value(), "Request conflicts with existing data", request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        if (ex instanceof ErrorResponse error && error.getStatusCode().is4xxClientError()) {
            int status = error.getStatusCode().value();
            return build(status, HttpStatus.valueOf(status).getReasonPhrase(), request, List.of());
        }
        log.error("Unhandled API exception on {} ({})", request.getRequestURI(), ex.getClass().getSimpleName(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error", request, List.of());
    }

    private ResponseEntity<ApiError> build(int status, String message, HttpServletRequest request,
                                           List<ValidationError> validationErrors) {
        String requestId = requestId(request);
        ApiError body = new ApiError(
                Instant.now(),
                status,
                HttpStatus.valueOf(status).getReasonPhrase(),
                message,
                request.getRequestURI(),
                requestId,
                validationErrors
        );
        return ResponseEntity.status(status)
                .header(REQUEST_ID_HEADER, requestId)
                .body(body);
    }

    private String requestId(HttpServletRequest request) {
        String supplied = request.getHeader(REQUEST_ID_HEADER);
        if (supplied != null) {
            supplied = supplied.trim();
            if (SAFE_REQUEST_ID.matcher(supplied).matches()) {
                return supplied;
            }
        }
        return UUID.randomUUID().toString();
    }

    private String reason(ResponseStatusException ex, String fallback) {
        String reason = ex.getReason();
        return reason == null || reason.isBlank() ? fallback : reason;
    }

    private String safeMessage(FieldError error) {
        return error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage();
    }

    public record ApiError(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            String requestId,
            List<ValidationError> validationErrors
    ) {}

    public record ValidationError(String field, String message) {}
}
