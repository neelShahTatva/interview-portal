package com.tatvasoft.interview_portal.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Bean
    public OpenAPI customOpenAPI() {
        List<Server> servers = new ArrayList<>();

        return new OpenAPI()
                .servers(servers)
                .info(new Info()
                        .title("AI Interview Portal API")
                        .version("1.0.0")
                        .description("REST API documentation for the AI-Powered Interview Portal application (" 
                                + activeProfile.toUpperCase() + " environment). Provides endpoints for candidate tracking, assessment creation, question bank management, AI code evaluation, and dashboard analytics.")
                        .contact(new Contact()
                                .name("TatvaSoft")
                                .email("support@tatvasoft.com")
                                .url("https://www.tatvasoft.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Provide JWT Bearer token obtained from `/auth/login` to access secured endpoints.")));
    }

    @Bean 
	public OpenApiCustomizer authControllerOpenApiCustomizer() {
        return openApi -> {
            if (activeProfile.equalsIgnoreCase("prod")) {
                if (openApi.getPaths() != null) {
                    openApi.getPaths().keySet().removeIf(path -> path.startsWith("/auth"));
                }
                if (openApi.getTags() != null) {
                    openApi.getTags().removeIf(tag -> "Authentication".equalsIgnoreCase(tag.getName()));
                }
            }
        };
    }
}
