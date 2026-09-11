package com.tatvasoft.interview_portal.controller;

import com.tatvasoft.interview_portal.dto.RoleResponseDto;
import com.tatvasoft.interview_portal.service.CommonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/common")
@RequiredArgsConstructor
public class CommonController {

    private final CommonService commonService;

    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponseDto>> getAllRoles(){
        List<RoleResponseDto> roles = commonService.getAllRoles();
        return ResponseEntity.ok(roles);
    }
}
