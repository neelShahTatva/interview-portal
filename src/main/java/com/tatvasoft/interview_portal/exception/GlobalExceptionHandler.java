package com.tatvasoft.interview_portal.exception;

import com.tatvasoft.interview_portal.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        ApiResponse<Object> response = new ApiResponse<>(400, false, List.of(ex.getMessage()), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(ResourceNotFoundException ex) {
        ApiResponse<Object> response = new ApiResponse<>(404, false, List.of(ex.getMessage()), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // Handles bad passwords or emails
    @ExceptionHandler({InvalidCredentialsException.class, AccountInactiveException.class})
    public ResponseEntity<ApiResponse<Object>> handleUnauthorized(RuntimeException ex) {
        ApiResponse<Object> response = new ApiResponse<>(401, false, List.of(ex.getMessage()), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // Handles expired or invalid tokens
    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleTokenValidation(TokenValidationException ex) {
        ApiResponse<Object> response = new ApiResponse<>(400, false, List.of(ex.getMessage()), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BulkUploadValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleBulkUploadValidation(BulkUploadValidationException ex) {
        ApiResponse<Object> response = new ApiResponse<>(
                400,
                false,
                splitValidationMessages(ex.getMessage()),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(Exception ex) {
        ApiResponse<Object> response = new ApiResponse<>(500, false, List.of(ex.getMessage()), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private List<String> splitValidationMessages(String message) {
        if (message == null || message.isBlank()) {
            return List.of();
        }

        String[] messages = message.split("\\s*\\|\\s*");
        return java.util.Arrays.stream(messages)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new LinkedHashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.putIfAbsent(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        List<String> errorMessages = errors.entrySet()
                .stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .toList();

        ApiResponse<Object> response =
                new ApiResponse<>(
                        400,
                        false,
                        errorMessages,
                        null
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}