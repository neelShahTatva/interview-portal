package com.tatvasoft.interview_portal.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleResponseDto {
    public Long id;
    public String roleName;
}
