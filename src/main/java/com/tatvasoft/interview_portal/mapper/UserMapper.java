package com.tatvasoft.interview_portal.mapper;

import com.tatvasoft.interview_portal.dto.UserRequest;
import com.tatvasoft.interview_portal.dto.UserResponse;
import com.tatvasoft.interview_portal.entity.Role;
import com.tatvasoft.interview_portal.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;

@Mapper(
        componentModel = "spring",
        imports = {LocalDateTime.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE // Automatically ignores unmapped fields like ID and tokens
)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", source = "encodedPassword")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "createdBy", source = "currentUserId")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    User toEntity(UserRequest request, String encodedPassword, Role role, Long currentUserId);

    @Mapping(target = "roleId", source = "role.id")
    UserResponse toResponse(User user);
}