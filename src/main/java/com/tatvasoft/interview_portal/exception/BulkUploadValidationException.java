package com.tatvasoft.interview_portal.exception;

public class BulkUploadValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BulkUploadValidationException(String message) {
        super(message);
    }

    public BulkUploadValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
