package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request payload for updating the logged-in user profile")
public class ProfileUpdateRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])[a-zA-Z0-9]+(?: [a-zA-Z0-9]+)*$",
        message = "Enter a valid username using letters, numbers and spaces only"
    )
    @Schema(description = "Updated username", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        message = "Please provide a valid email address"
    )
    @Size(max = 255, message = "Email should not exceed 255 characters")
    @Schema(description = "Updated email address", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "Optional updated password")
    private String password;

    @Schema(description = "Role ID", example = "1")
    private Long roleId;

    @Schema(description = "Active status", example = "true")
    private Boolean isActive;
}
