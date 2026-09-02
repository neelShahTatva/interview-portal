package com.tatvasoft.interview_portal.enums;

import java.util.Locale;

public enum QuestionDifficultyLevel {
    EASY,
    MEDIUM,
    HARD;

    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "EASY", "MEDIUM", "HARD" -> true;
            default -> false;
        };
    }
}
