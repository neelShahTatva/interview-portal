package com.tatvasoft.interview_portal.util;

import com.tatvasoft.interview_portal.enums.FileContentType;
import com.tatvasoft.interview_portal.enums.FileExtension;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;

@Component
public class FileValidationUtil {

    private static final long MAX_PROFILE_PICTURE_SIZE =
            5 * 1024 * 1024; // 5 MB

    public String validateProfilePicture(
            MultipartFile file) {

        validateFilePresent(file);
        validateFileSize(file);
        validateContentType(file);

        String extension =
                validateExtension(file);

        validateFileContent(
                file,
                extension
        );

        return extension;
    }

    private void validateFilePresent(
            MultipartFile file) {

        if (file == null || file.isEmpty()) {

            throw new IllegalArgumentException(
                    "No file provided. Please select an image to upload."
            );
        }
    }

    private void validateFileSize(
            MultipartFile file) {

        if (file.getSize() > MAX_PROFILE_PICTURE_SIZE) {

            throw new IllegalArgumentException(
                    "File size exceeds the 5 MB limit. "
                            + "Please upload a smaller image."
            );
        }
    }

    private void validateContentType(
            MultipartFile file) {

        String contentType =
                file.getContentType();

        if (!FileContentType.isValid(contentType)) {

            throw new IllegalArgumentException(
                    "Invalid file type. "
                            + "Only JPEG, PNG, and WEBP images are allowed."
            );
        }
    }

    private String validateExtension(
            MultipartFile file) {

        String originalFilename =
                file.getOriginalFilename();

        if (originalFilename == null
                || !originalFilename.contains(".")) {

            throw new IllegalArgumentException(
                    "File must have a valid extension "
                            + "(jpg, jpeg, png, webp)."
            );
        }

        String extension =
                getExtension(originalFilename);

        if (!FileExtension.isValid(extension)) {

            throw new IllegalArgumentException(
                    "Invalid file extension. "
                            + "Allowed: jpg, jpeg, png, webp."
            );
        }

        return extension;
    }

    private void validateFileContent(
            MultipartFile file,
            String extension) {

        byte[] fileBytes;

        try {

            fileBytes = file.getBytes();

        } catch (IOException e) {

            throw new IllegalArgumentException(
                    "Could not read file content. "
                            + "Please try again.",
                    e
            );
        }

        if (!hasValidImageSignature(
                fileBytes,
                extension)) {

            throw new IllegalArgumentException(
                    "File content does not match its declared type. "
                            + "Please upload a real image."
            );
        }
    }

    private boolean hasValidImageSignature(
            byte[] fileBytes,
            String extension) {

        if (fileBytes == null
                || fileBytes.length < 4) {

            return false;
        }

        byte[] header =
                Arrays.copyOf(fileBytes, 4);

        return switch (extension) {

            case "jpg", "jpeg" ->
                    (header[0] & 0xFF) == 0xFF
                            && (header[1] & 0xFF) == 0xD8
                            && (header[2] & 0xFF) == 0xFF;

            case "png" ->
                    (header[0] & 0xFF) == 0x89
                            && (header[1] & 0xFF) == 0x50
                            && (header[2] & 0xFF) == 0x4E
                            && (header[3] & 0xFF) == 0x47;

            case "webp" ->
                    (header[0] & 0xFF) == 0x52
                            && (header[1] & 0xFF) == 0x49
                            && (header[2] & 0xFF) == 0x46
                            && (header[3] & 0xFF) == 0x46;

            default -> false;
        };
    }

    public String getExtension(
            String filename) {

        if (filename == null) {
            return "";
        }

        int lastDot =
                filename.lastIndexOf('.');

        if (lastDot < 0
                || lastDot == filename.length() - 1) {

            return "";
        }

        return filename
                .substring(lastDot + 1)
                .toLowerCase();
    }
}