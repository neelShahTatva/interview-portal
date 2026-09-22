package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response payload representing a user")
public class UserResponse {

    @Schema(description = "User unique ID", example = "1")
    private Long id;

    @Schema(description = "Username", example = "john_doe")
    private String username;

    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Role ID assigned to user", example = "2")
    private Long roleId;

    @Schema(description = "Account active status", example = "true")
    private Boolean isActive;

    @Schema(description = "Account creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Profile picture filename / path", example = "avatar.png")
    private String profilePicture;
}