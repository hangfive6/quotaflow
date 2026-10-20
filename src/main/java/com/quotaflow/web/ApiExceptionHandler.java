package com.quotaflow.web;

import com.quotaflow.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import static com.quotaflow.web.ApiModels.ErrorResponse;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ErrorResponse> notFound(NotFoundException ex, HttpServletRequest req) { return error(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), req); }
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ErrorResponse> business(BusinessException ex, HttpServletRequest req) { return error(HttpStatus.CONFLICT, ex.getCode(), ex.getMessage(), req); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var message = ex.getBindingResult().getFieldErrors().stream().findFirst().map(e -> e.getField() + ": " + e.getDefaultMessage()).orElse("Invalid request");
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, req);
    }
    private ResponseEntity<ErrorResponse> error(HttpStatus status, String code, String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message, (String) req.getAttribute("requestId"), Instant.now()));
    }
}

