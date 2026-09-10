package com.tatvasoft.interview_portal.service.impl;

import com.tatvasoft.interview_portal.dto.RoleResponseDto;
import com.tatvasoft.interview_portal.entity.Role;
import com.tatvasoft.interview_portal.repository.RoleRepository;
import com.tatvasoft.interview_portal.service.CommonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommonServiceImpl implements CommonService {

    private final RoleRepository roleRepository;

    @Override
    public List<RoleResponseDto> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(role -> RoleResponseDto.builder()
                        .id(role.getId())
                        .roleName(role.getRoleName())
                        .build())
                .toList();
    }
}
