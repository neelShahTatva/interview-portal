package com.tatvasoft.interview_portal.exception;

public class FileStorageException
        extends RuntimeException {

    public FileStorageException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}