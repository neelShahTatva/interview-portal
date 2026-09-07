package com.tatvasoft.interview_portal.enums;

import java.util.Arrays;
import java.util.Locale;

public enum QuestionUploadHeader {
    TITLE("Title"),
    DESCRIPTION("Description"),
    DIFFICULTY("Difficulty"),
    ESTIMATED_TIME("Estimated Time"),
    IS_ACTIVE("Is Active"),
    CATEGORIES("Categories"),
    DESIGNATIONS("Designations"),
    JAVA_SOLUTION("Java Solution");

    private final String displayName;

    QuestionUploadHeader(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static String[] getRequiredHeaders() {
        return Arrays.stream(values())
                .map(QuestionUploadHeader::getDisplayName)
                .toArray(String[]::new);
    }

    public static String normalizeHeader(String value) {
        if (value == null) {
            return "";
        }

        return value.replaceAll("[^a-zA-Z0-9]", "")
                .toLowerCase(Locale.ROOT);
    }
}
