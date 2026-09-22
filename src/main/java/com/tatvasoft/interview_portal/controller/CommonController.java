package com.tatvasoft.interview_portal.controller;

import com.tatvasoft.interview_portal.dto.RoleResponseDto;
import com.tatvasoft.interview_portal.service.CommonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/common")
@RequiredArgsConstructor
@Tag(name = "Common", description = "Common system lookup and reference data endpoints")
public class CommonController {

    private final CommonService commonService;

    @Operation(summary = "Get All User Roles", description = "Retrieves the list of all available user roles in the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles retrieved successfully")
    })
    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponseDto>> getAllRoles(){
        List<RoleResponseDto> roles = commonService.getAllRoles();
        return ResponseEntity.ok(roles);
    }
}
