package com.tatvasoft.interview_portal.controller;

import com.tatvasoft.interview_portal.dto.ApiResponse;
import com.tatvasoft.interview_portal.entity.Role;
import com.tatvasoft.interview_portal.repository.RoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/common")
public class CommonController {

    private final RoleRepository roleRepository;

    public CommonController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        return ResponseEntity.ok(
                new ApiResponse<>(200, true, null, roleRepository.findAll())
        );
    }
}
