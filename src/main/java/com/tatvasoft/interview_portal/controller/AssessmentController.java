package com.tatvasoft.interview_portal.controller;

import com.tatvasoft.interview_portal.dto.AssessmentRequest;
import com.tatvasoft.interview_portal.dto.AssessmentResponse;
import com.tatvasoft.interview_portal.dto.CandidateResponse;
import com.tatvasoft.interview_portal.service.AssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assessments")
@Tag(name = "Assessment Management", description = "Endpoints for creating, managing, and tracking candidate assessments")
public class AssessmentController {

    private final AssessmentService service;

    public AssessmentController(AssessmentService service) {
        this.service = service;
    }

    @Operation(summary = "Create Assessment", description = "Creates a new assessment with selected questions for a candidate.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assessment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid assessment request payload"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<AssessmentResponse>> create(
            @Valid @RequestBody AssessmentRequest request) {

        return ResponseEntity.ok(
                new com.tatvasoft.interview_portal.dto.ApiResponse<>(
                        200,
                        true,
                        null,
                        service.create(request)
                )
        );
    }

    @Operation(summary = "Get All Assessments", description = "Retrieves a list of all assessments.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of assessments retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<List<AssessmentResponse>>> getAll() {

        return ResponseEntity.ok(
                new com.tatvasoft.interview_portal.dto.ApiResponse<>(
                        200,
                        true,
                        null,
                        service.getAll()
                )
        );
    }

    @Operation(summary = "Get Available Candidates", description = "Retrieves candidates that are available for assessment assignment.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Available candidates retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/available-candidates")
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<List<CandidateResponse>>>
    getAvailableCandidates() {

        return ResponseEntity.ok(
                new com.tatvasoft.interview_portal.dto.ApiResponse<>(
                        200,
                        true,
                        null,
                        service.getAvailableCandidates()
                )
        );
    }

    @Operation(summary = "Get Assessment by ID", description = "Fetches assessment details and assigned questions by assessment ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assessment details found"),
            @ApiResponse(responseCode = "400", description = "Invalid assessment ID supplied"),
            @ApiResponse(responseCode = "404", description = "Assessment not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<AssessmentResponse>> get(
            @Parameter(description = "Unique ID of the assessment", required = true) @PathVariable Long id) {

        return ResponseEntity.ok(
                new com.tatvasoft.interview_portal.dto.ApiResponse<>(
                        200,
                        true,
                        null,
                        service.getById(id)
                )
        );
    }

    @Operation(summary = "Deactivate/Delete Assessment", description = "Deactivates an assessment by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assessment deactivated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid assessment ID supplied"),
            @ApiResponse(responseCode = "404", description = "Assessment not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<String>> delete(
            @Parameter(description = "Unique ID of the assessment", required = true) @PathVariable Long id) {

        service.delete(id);

        return ResponseEntity.ok(
                new com.tatvasoft.interview_portal.dto.ApiResponse<>(
                        200,
                        true,
                        null,
                        "Assessment deactivated"
                )
        );
    }

    @Operation(summary = "Update Assessment Status", description = "Updates the lifecycle status of an assessment (e.g., PENDING, IN_PROGRESS, COMPLETED).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status value or parameters"),
            @ApiResponse(responseCode = "404", description = "Assessment not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{id}/status")
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<String>>
    changeStatus(
            @Parameter(description = "Unique ID of the assessment", required = true) @PathVariable Long id,
            @Parameter(description = "New status for the assessment", required = true) @RequestParam String status) {

        service.changeStatus(
                id,
                status
        );

        return ResponseEntity.ok(
                new com.tatvasoft.interview_portal.dto.ApiResponse<>(
                        200,
                        true,
                        null,
                        "Status updated successfully"
                )
        );
    }
}