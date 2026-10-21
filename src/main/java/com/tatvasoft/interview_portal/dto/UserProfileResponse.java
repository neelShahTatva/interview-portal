package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Response payload containing the user's detailed profile")
public class UserProfileResponse {

    @Schema(description = "User unique ID", example = "1")
    private Long id;

    @Schema(description = "Username", example = "john_doe")
    private String username;

    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Role ID", example = "1")
    private Long roleId;

    @Schema(description = "Role name", example = "ADMIN")
    private String roleName;

    @Schema(description = "Account active status", example = "true")
    private Boolean isActive;

    @Schema(description = "Account creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Profile picture URL or path", example = "avatar.jpg")
    private String profilePictureUrl;

    @Schema(description = "User token if applicable")
    private String token;
}
