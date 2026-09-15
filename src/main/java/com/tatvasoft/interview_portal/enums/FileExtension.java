package com.tatvasoft.interview_portal.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum FileExtension {
    JPG("jpg"),
    JPEG("jpeg"),
    PNG("png"),
    WEBP("webp");

    private final String value;

    FileExtension(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean isValid(String extension) {
        if (extension == null || extension.isBlank()) {
            return false;
        }
        String normalized = extension.trim().toLowerCase();
        return Arrays.stream(values())
                .anyMatch(ext -> ext.value.equalsIgnoreCase(normalized));
    }

    public static Set<String> getAllowedValues() {
        return Arrays.stream(values())
                .map(FileExtension::getValue)
                .collect(Collectors.toUnmodifiableSet());
    }
}
