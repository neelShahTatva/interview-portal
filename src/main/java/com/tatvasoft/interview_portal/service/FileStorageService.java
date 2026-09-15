package com.tatvasoft.interview_portal.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {

    String save(
            MultipartFile file,
            String directory,
            String extension
    );

    byte[] fetch(
            String filename,
            String directory
    ) throws IOException;

    void delete(
            String filename,
            String directory
    ) throws IOException;

    Path getPath(
            String filename,
            String directory
    );
}