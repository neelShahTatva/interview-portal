package com.tatvasoft.interview_portal.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum FileContentType {
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png"),
    IMAGE_WEBP("image/webp");

    private final String value;

    FileContentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean isValid(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return false;
        }
        String normalized = contentType.trim().toLowerCase();
        return Arrays.stream(values())
                .anyMatch(ct -> ct.value.equalsIgnoreCase(normalized));
    }

    public static Set<String> getAllowedValues() {
        return Arrays.stream(values())
                .map(FileContentType::getValue)
                .collect(Collectors.toUnmodifiableSet());
    }
}
