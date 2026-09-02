package com.tatvasoft.interview_portal.enums;

import java.util.Locale;

public enum QuestionDesignationType {
    TSE,
    ASE,
    SE,
    SSE,
    TL,
    STL,
    APM,
    PM,
    PPM;

    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "TSE", "ASE", "SE", "SSE", "TL", "STL", "APM", "PM", "PPM" -> true;
            default -> false;
        };
    }
}
