package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Role response details")
public class RoleResponseDto {

    @Schema(description = "Role unique ID", example = "1")
    public Long id;

    @Schema(description = "Role name", example = "ADMIN")
    public String roleName;
}
