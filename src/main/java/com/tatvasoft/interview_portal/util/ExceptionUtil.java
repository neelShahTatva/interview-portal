package com.tatvasoft.interview_portal.util;

import com.tatvasoft.interview_portal.exception.globalException.CommonException;
import org.springframework.http.HttpStatus;

import java.util.List;

public final class ExceptionUtil {

    public static void buildErrorResponse(
            HttpStatus status,
            String message) {

        throw new CommonException(status, message);
    }

    public static void buildErrorResponse(
            HttpStatus status,
            List<String> messages) {

        throw new CommonException(status, messages);
    }
}