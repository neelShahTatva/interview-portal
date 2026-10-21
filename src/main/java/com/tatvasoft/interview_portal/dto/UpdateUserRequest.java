package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request payload for updating user details")
public class UpdateUserRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])[a-zA-Z0-9]+(?:[._][a-zA-Z0-9]+)*$",
            message = "Username must contain at least one letter and may contain only letters, numbers, dots (.) and underscores (_)"
    )
    @Schema(description = "Updated username", example = "john_doe_updated", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 255, message = "Email should not exceed 255 characters")
    @Schema(description = "Updated email address", example = "john.updated@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "Role ID assigned to user", example = "2")
    private Long roleId;

    @Schema(description = "Account active status", example = "true")
    private Boolean isActive;
}
