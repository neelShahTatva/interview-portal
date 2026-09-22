package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request payload to set a new password using reset token")
public class ResetPasswordRequest {

    @NotBlank(message = "Token is required")
    @Schema(description = "Password reset verification token", example = "4e3d2c1b-a012-4def-9876-543210fedcba", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 64 characters")
    @Pattern(
            regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    @Schema(description = "New password (min 8 chars, including uppercase, lowercase, number, and special character)", example = "NewSecret@2026", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;
}