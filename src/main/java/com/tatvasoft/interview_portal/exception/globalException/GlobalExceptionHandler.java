package com.tatvasoft.interview_portal.exception.globalException;

import com.tatvasoft.interview_portal.dto.ApiResponse;
import com.tatvasoft.interview_portal.entity.GlobalException;
import com.tatvasoft.interview_portal.entity.User;
import com.tatvasoft.interview_portal.exception.*;
import com.tatvasoft.interview_portal.repository.GlobalExceptionRepository;
import com.tatvasoft.interview_portal.repository.UserRepository;
import com.tatvasoft.interview_portal.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final GlobalExceptionRepository globalExceptionRepository;
    private final UserRepository userRepository;

    @Autowired
    public GlobalExceptionHandler(
            GlobalExceptionRepository globalExceptionRepository,
            UserRepository userRepository) {

        this.globalExceptionRepository = globalExceptionRepository;
        this.userRepository = userRepository;
    }

    public GlobalExceptionHandler() {
        this.globalExceptionRepository = null;
        this.userRepository = null;
    }

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<ApiResponse<GlobalException>> handleCommonException(
            CommonException ex,
            HttpServletRequest request) {

        log.warn("Common application exception: {}", ex.getMessage());

        HttpStatus status = ex.getStatus();

        List<String> messages = ex.getErrorMessages();

        if (messages == null || messages.isEmpty()) {
            messages = List.of(
                    ex.getMessage() != null
                            ? ex.getMessage()
                            : "An unexpected error occurred"
            );
        }

        String primaryMessage = String.join("; ", messages);

        GlobalException logEntity =
                createAndSaveExceptionLog(
                        status.value(),
                        primaryMessage,
                        ex,
                        request
                );

        ApiResponse<GlobalException> response =
                ApiResponse.<GlobalException>builder()
                        .statusCode(status.value())
                        .success(false)
                        .errorMessages(messages)
                        .result(logEntity)
                        .build();

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<GlobalException>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        log.warn("Validation error: {}", ex.getMessage());

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
                .map(entry ->
                        entry.getKey() + ": " + entry.getValue()
                )
                .toList();

        if (errorMessages.isEmpty()) {
            errorMessages = List.of("Validation failed");
        }

        String primaryMessage = String.join(
                "; ",
                errorMessages
        );

        GlobalException logEntity =
                createAndSaveExceptionLog(
                        HttpStatus.BAD_REQUEST.value(),
                        primaryMessage,
                        ex,
                        request
                );

        ApiResponse<GlobalException> response =
                ApiResponse.<GlobalException>builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .errorMessages(errorMessages)
                        .result(logEntity)
                        .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

//      Handles @RequestParam / @PathVariable constraint violations.

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<GlobalException>>
    handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        log.warn("Constraint violation: {}", ex.getMessage());

        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .toList();

        if (errors.isEmpty()) {
            errors = List.of("Constraint violation");
        }

        String primaryMessage = String.join(
                "; ",
                errors
        );

        GlobalException logEntity =
                createAndSaveExceptionLog(
                        HttpStatus.BAD_REQUEST.value(),
                        primaryMessage,
                        ex,
                        request
                );

        ApiResponse<GlobalException> response =
                ApiResponse.<GlobalException>builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .errorMessages(errors)
                        .result(logEntity)
                        .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }



//      Handles malformed JSON / invalid request body.

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<GlobalException>>
    handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        log.warn("Malformed JSON request: {}", ex.getMessage());

        String message =
                "Malformed JSON request or invalid request body format";

        GlobalException logEntity =
                createAndSaveExceptionLog(
                        HttpStatus.BAD_REQUEST.value(),
                        message,
                        ex,
                        request
                );

        ApiResponse<GlobalException> response =
                ApiResponse.<GlobalException>builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .errorMessages(List.of(message))
                        .result(logEntity)
                        .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }



//     Handles missing request parameters.

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<GlobalException>>
    handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        log.warn(
                "Missing parameter: {}",
                ex.getParameterName()
        );

        String message = String.format(
                "Required request parameter '%s' of type %s is missing",
                ex.getParameterName(),
                ex.getParameterType()
        );

        GlobalException logEntity =
                createAndSaveExceptionLog(
                        HttpStatus.BAD_REQUEST.value(),
                        message,
                        ex,
                        request
                );

        ApiResponse<GlobalException> response =
                ApiResponse.<GlobalException>builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .errorMessages(List.of(message))
                        .result(logEntity)
                        .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<GlobalException>>
    handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        log.warn(
                "Type mismatch for parameter: {}",
                ex.getName()
        );

        String requiredType =
                ex.getRequiredType() != null
                        ? ex.getRequiredType().getSimpleName()
                        : "valid type";

        String message = String.format(
                "Parameter '%s' should be of type %s",
                ex.getName(),
                requiredType
        );

        GlobalException logEntity =
                createAndSaveExceptionLog(
                        HttpStatus.BAD_REQUEST.value(),
                        message,
                        ex,
                        request
                );

        ApiResponse<GlobalException> response =
                ApiResponse.<GlobalException>builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .errorMessages(List.of(message))
                        .result(logEntity)
                        .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<GlobalException>>
    handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request) {

        log.warn(
                "Maximum upload size exceeded: {}",
                ex.getMessage()
        );

        String message =
                "Maximum upload size exceeded. File is too large.";

        GlobalException logEntity =
                createAndSaveExceptionLog(
                        HttpStatus.BAD_REQUEST.value(),
                        message,
                        ex,
                        request
                );

        ApiResponse<GlobalException> response =
                ApiResponse.<GlobalException>builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .errorMessages(List.of(message))
                        .result(logEntity)
                        .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<GlobalException>>
    handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn(
                "Illegal argument: {}",
                ex.getMessage()
        );

        String message =
                (ex.getMessage() != null &&
                        !ex.getMessage().isBlank())
                        ? ex.getMessage()
                        : "Invalid input provided";

        GlobalException logEntity =
                createAndSaveExceptionLog(
                        HttpStatus.BAD_REQUEST.value(),
                        message,
                        ex,
                        request
                );

        ApiResponse<GlobalException> response =
                ApiResponse.<GlobalException>builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .errorMessages(List.of(message))
                        .result(logEntity)
                        .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<ApiResponse<GlobalException>>
    handleTokenValidation(
            TokenValidationException ex,
            HttpServletRequest request) {

        log.warn(
                "Token validation failed: {}",
                ex.getMessage()
        );

        String message =
                ex.getMessage() != null
                        ? ex.getMessage()
                        : "Token validation failed";

        GlobalException logEntity =
                createAndSaveExceptionLog(
                        HttpStatus.BAD_REQUEST.value(),
                        message,
                        ex,
                        request
                );

        ApiResponse<GlobalException> response =
                ApiResponse.<GlobalException>builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .errorMessages(List.of(message))
                        .result(logEntity)
                        .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<GlobalException>>
    handleUserAlreadyExists(
            UserAlreadyExistsException ex,
            HttpServletRequest request) {

        log.warn(
                "User already exists: {}",
                ex.getMessage()
        );

        String message =
                ex.getMessage() != null
                        ? ex.getMessage()
                        : "User already exists";

        GlobalException logEntity =
                createAndSaveExceptionLog(
                        HttpStatus.BAD_REQUEST.value(),
                        message,
                        ex,
                        request
                );

        ApiResponse<GlobalException> response =
                ApiResponse.<GlobalException>builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .errorMessages(List.of(message))
                        .result(logEntity)
                        .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(BulkUploadValidationException.class)
    public ResponseEntity<ApiResponse<GlobalException>>
    handleBulkUploadValidation(
            BulkUploadValidationException ex,
            HttpServletRequest request) {

        log.warn(
                "Bulk upload validation failed: {}",
                ex.getMessage()
        );

        List<String> messages =
                splitValidationMessages(ex.getMessage());

        if (messages.isEmpty()) {
            messages = List.of(
                    "Bulk upload validation failed"
            );
        }

        String primaryMessage =
                String.join("; ", messages);

        GlobalException logEntity =
                createAndSaveExceptionLog(
                        HttpStatus.BAD_REQUEST.value(),
                        primaryMessage,
                        ex,
                        request
                );

        ApiResponse<GlobalException> response =
                ApiResponse.<GlobalException>builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .errorMessages(messages)
                        .result(logEntity)
                        .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler({
            InvalidCredentialsException.class,
            AccountInactiveException.class
    })
    public ResponseEntity<ApiResponse<GlobalException>>
    handleUnauthorized(
            RuntimeException ex,
            HttpServletRequest request) {

        log.warn(
                "Unauthorized access: {}",
                ex.getMessage()
        );

        String message =
                ex.getMessage() != null
                        ? ex.getMessage()
                        : "Unauthorized access";

        GlobalException logEntity =
                createAndSaveExceptionLog(
                        HttpStatus.UNAUTHORIZED.value(),
                        message,
                        ex,
                        request
                );

        ApiResponse<GlobalException> response =
                ApiResponse.<GlobalException>builder()
                        .statusCode(
                                HttpStatus.UNAUTHORIZED.value()
                        )
                        .success(false)
                        .errorMessages(List.of(message))
                        .result(logEntity)
                        .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<GlobalException>>
    handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.warn(
                "Resource not found: {}",
                ex.getMessage()
        );

        String message =
                ex.getMessage() != null
                        ? ex.getMessage()
                        : "Resource not found";

        GlobalException logEntity =
                createAndSaveExceptionLog(
                        HttpStatus.NOT_FOUND.value(),
                        message,
                        ex,
                        request
                );

        ApiResponse<GlobalException> response =
                ApiResponse.<GlobalException>builder()
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .errorMessages(List.of(message))
                        .result(logEntity)
                        .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(AssessmentException.class)
    public ResponseEntity<ApiResponse<GlobalException>>
    handleAssessmentException(
            AssessmentException ex,
            HttpServletRequest request) {

        log.warn(
                "Assessment conflict: {}",
                ex.getMessage()
        );

        String message =
                ex.getMessage() != null
                        ? ex.getMessage()
                        : "Assessment conflict";

        GlobalException logEntity =
                createAndSaveExceptionLog(
                        HttpStatus.CONFLICT.value(),
                        message,
                        ex,
                        request
                );

        ApiResponse<GlobalException> response =
                ApiResponse.<GlobalException>builder()
                        .statusCode(HttpStatus.CONFLICT.value())
                        .success(false)
                        .errorMessages(List.of(message))
                        .result(logEntity)
                        .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

//    500 file storage exception

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiResponse<GlobalException>>
    handleFileStorageException(
            FileStorageException ex,
            HttpServletRequest request) {

        log.error(
                "File storage error: ",
                ex
        );

        String message =
                (ex.getMessage() != null &&
                        !ex.getMessage().isBlank())
                        ? ex.getMessage()
                        : "File storage error occurred";

        GlobalException logEntity =
                createAndSaveExceptionLog(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        message,
                        ex,
                        request
                );

        ApiResponse<GlobalException> response =
                ApiResponse.<GlobalException>builder()
                        .statusCode(
                                HttpStatus.INTERNAL_SERVER_ERROR.value()
                        )
                        .success(false)
                        .errorMessages(List.of(message))
                        .result(logEntity)
                        .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }


    // 500 - any unexpected exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<GlobalException>>
    handleGeneric(
            Exception ex,
            HttpServletRequest request) {

        log.error(
                "Unhandled exception caught by global handler: ",
                ex
        );

        String message =
                (ex.getMessage() != null &&
                        !ex.getMessage().isBlank())
                        ? ex.getMessage()
                        : "Internal Server Error";

        GlobalException logEntity =
                createAndSaveExceptionLog(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        message,
                        ex,
                        request
                );

        ApiResponse<GlobalException> response =
                ApiResponse.<GlobalException>builder()
                        .statusCode(
                                HttpStatus.INTERNAL_SERVER_ERROR.value()
                        )
                        .success(false)
                        .errorMessages(List.of(message))
                        .result(logEntity)
                        .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }


    private GlobalException createAndSaveExceptionLog(
            int errorCode,
            String errorMessage,
            Throwable throwable,
            HttpServletRequest request) {

        GlobalException logEntity =
                GlobalException.builder()
                        .errorCode(errorCode)
                        .errorMessage(errorMessage)
                        .stackTrace(
                                getStackTraceAsString(throwable)
                        )
                        .userId(getCurrentUserId())
                        .apiEndpoint(getApiEndpoint(request))
                        .timestamp(LocalDateTime.now())
                        .build();

        if (globalExceptionRepository != null) {
            try {
                return globalExceptionRepository.save(
                        logEntity
                );
            } catch (Exception e) {
                log.error(
                        "Failed to persist GlobalException log to database: {}",
                        e.getMessage()
                );
            }
        }

        return logEntity;
    }


    private Long getCurrentUserId() {

        try {

            Long userId =
                    SecurityUtil.getCurrentUserId();

            if (userId != null) {
                return userId;
            }

            String username =
                    SecurityUtil.getCurrentUsername();

            if (username != null &&
                    !"anonymousUser".equalsIgnoreCase(username) &&
                    userRepository != null) {

                return userRepository
                        .findByUsername(username)
                        .map(User::getId)
                        .orElse(null);
            }

        } catch (Exception e) {

            log.warn(
                    "Could not resolve current user ID: {}",
                    e.getMessage()
            );
        }

        return null;
    }


    private String getApiEndpoint(
            HttpServletRequest request) {

        if (request == null) {
            return "N/A";
        }

        String method =
                request.getMethod() != null
                        ? request.getMethod()
                        : "UNKNOWN";

        String uri =
                request.getRequestURI() != null
                        ? request.getRequestURI()
                        : "";

        String query =
                request.getQueryString();

        if (query != null &&
                !query.isBlank()) {

            uri += "?" + query;
        }

        return method + " " + uri;
    }


    private String getStackTraceAsString(
            Throwable throwable) {

        if (throwable == null) {
            return "";
        }

        StringWriter sw =
                new StringWriter();

        PrintWriter pw =
                new PrintWriter(sw);

        throwable.printStackTrace(pw);

        return sw.toString();
    }


    private List<String> splitValidationMessages(
            String message) {

        if (message == null ||
                message.isBlank()) {

            return List.of();
        }

        String[] messages =
                message.split("\\s*\\|\\s*");

        return Arrays.stream(messages)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<GlobalException>>
    handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {

        log.warn(
                "Unsupported Content-Type: {}",
                ex.getContentType()
        );

        String message =
                "Unsupported Content-Type. Please use application/json.";

        GlobalException logEntity =
                createAndSaveExceptionLog(
                        HttpStatus.BAD_REQUEST.value(),
                        message,
                        ex,
                        request
                );

        ApiResponse<GlobalException> response =
                ApiResponse.<GlobalException>builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .errorMessages(List.of(message))
                        .result(logEntity)
                        .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}