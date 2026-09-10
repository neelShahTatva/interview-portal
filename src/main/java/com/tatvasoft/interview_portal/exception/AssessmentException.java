package com.tatvasoft.interview_portal.exception;

public class AssessmentException extends RuntimeException {

    private final String errorCode;

    public AssessmentException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
