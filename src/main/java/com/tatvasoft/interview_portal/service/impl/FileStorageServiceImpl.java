package com.tatvasoft.interview_portal.service.impl;

import com.tatvasoft.interview_portal.exception.FileStorageException;
import com.tatvasoft.interview_portal.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileStorageServiceImpl
        implements FileStorageService {

    private static final DateTimeFormatter
            FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "yyyyMMdd_HHmmss"
            );

    @Override
    public String save(
            MultipartFile file,
            String directory,
            String extension) {

        Path uploadPath =
                Paths.get(directory);

        try {

            Files.createDirectories(uploadPath);

            String filename =
                    generateFilename(extension);

            Path filePath =
                    uploadPath.resolve(filename);

        
            Files.write(
                    filePath,
                    file.getBytes()
            );

            return filename;

        } catch (IOException e) {

            throw new FileStorageException(
                    "Failed to save file.",
                    e
            );
        }
    }

    @Override
    public byte[] fetch(
            String filename,
            String directory) {

        try {

            Path filePath =
                    getPath(
                            filename,
                            directory
                    );

            if (!Files.exists(filePath)) {
                throw new FileStorageException(
                        "File not found: " + filename,
                        null
                );
            }

            return Files.readAllBytes(filePath);

        } catch (FileStorageException e) {

            throw e;

        } catch (IOException e) {

            throw new FileStorageException(
                    "Failed to read file.",
                    e
            );
        }
    }

    @Override
    public void delete(
            String filename,
            String directory) {

        if (filename == null
                || filename.isBlank()) {
            return;
        }

        try {

            Path filePath =
                    getPath(
                            filename,
                            directory
                    );

            Files.deleteIfExists(filePath);

        } catch (IOException e) {

            throw new FileStorageException(
                    "Failed to delete file.",
                    e
            );
        }
    }

    @Override
    public Path getPath(
            String filename,
            String directory) {

        if (filename == null
                || filename.isBlank()) {

            throw new IllegalArgumentException(
                    "Filename must not be empty."
            );
        }

        Path basePath =
                Paths.get(directory)
                        .toAbsolutePath()
                        .normalize();

        Path filePath =
                basePath
                        .resolve(filename)
                        .normalize();

      
        if (!filePath.startsWith(basePath)) {

            throw new IllegalArgumentException(
                    "Invalid file path."
            );
        }

        return filePath;
    }

    private String generateFilename(
            String extension) {

        String date =
                LocalDateTime.now()
                        .format(FILE_DATE_FORMAT);

        String uuid =
                UUID.randomUUID()
                        .toString()
                        .substring(0, 6);

        return date
                + "_"
                + uuid
                + "."
                + extension;
    }
}