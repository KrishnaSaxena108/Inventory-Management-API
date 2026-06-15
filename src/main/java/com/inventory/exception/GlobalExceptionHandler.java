package com.inventory.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(
            ResourceNotFoundException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", HttpStatus.NOT_FOUND.value());
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            BadRequestException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", HttpStatus.BAD_REQUEST.value());
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error ->
                        error.getDefaultMessage())
                .orElse("Validation failed");

        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", HttpStatus.BAD_REQUEST.value());
        response.put("message", errorMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(
            Exception ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}