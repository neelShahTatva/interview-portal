package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request payload to initiate forgot password flow")
public class ForgotPasswordRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 255, message = "Email should not exceed 255 characters")
    @Schema(description = "Registered email address to receive password reset link", example = "user@tatvasoft.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
}