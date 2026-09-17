package com.tatvasoft.interview_portal.exception.globalException;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public class CommonException extends RuntimeException {

    private final HttpStatus status;
    private final List<String> errorMessages;

    public CommonException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.errorMessages = List.of(message);
    }

    public CommonException(HttpStatus status, List<String> errorMessages) {
        super(String.join("; ", errorMessages));
        this.status = status;
        this.errorMessages = errorMessages;
    }
}