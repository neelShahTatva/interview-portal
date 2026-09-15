package com.tatvasoft.interview_portal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads/profile-pictures}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().getParent();
        try {
            java.nio.file.Files.createDirectories(uploadPath);
            java.nio.file.Files.createDirectories(Paths.get(uploadDir).toAbsolutePath());
        } catch (Exception ignored) {}
        String resourceLocation = uploadPath.toUri().toString();
        if (!resourceLocation.endsWith("/")) {
            resourceLocation += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);
    }
}
