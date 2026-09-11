package com.tatvasoft.interview_portal.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static String getCurrentUsername() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return null;
        }

        return authentication.getName();
    }

    public static Long getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return null;
        }

        Object details = authentication.getDetails();
        if (details instanceof Long userId) {
            return userId;
        }
        if (details instanceof Number number) {
            return number.longValue();
        }
        return null;
    }
}