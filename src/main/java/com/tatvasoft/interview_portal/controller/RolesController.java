package com.tatvasoft.interview_portal.controller;

import com.tatvasoft.interview_portal.dto.RoleResponseDto;
import com.tatvasoft.interview_portal.service.RolesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RolesController {

    private final RolesService rolesService;

    @GetMapping
    public ResponseEntity<List<RoleResponseDto>> getAllRoles(){
        List<RoleResponseDto> roles = rolesService.getAllRoles();
        return ResponseEntity.ok(roles);
    }
}
