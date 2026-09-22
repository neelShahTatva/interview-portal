package com.tatvasoft.interview_portal.controller;

import com.tatvasoft.interview_portal.dto.ApiResponse;
import com.tatvasoft.interview_portal.dto.AssessmentRequest;
import com.tatvasoft.interview_portal.dto.AssessmentResponse;
import com.tatvasoft.interview_portal.dto.CandidateResponse;
import com.tatvasoft.interview_portal.service.AssessmentService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assessment created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid assessment request payload")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<AssessmentResponse>> create(
            @Valid @RequestBody AssessmentRequest request) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        true,
                        null,
                        service.create(request)
                )
        );
    }

    @Operation(summary = "Get All Assessments", description = "Retrieves a list of all assessments.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "List of assessments retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<AssessmentResponse>>> getAll() {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        true,
                        null,
                        service.getAll()
                )
        );
    }

    @Operation(summary = "Get Available Candidates", description = "Retrieves candidates that are available for assessment assignment.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Available candidates retrieved successfully")
    })
    @GetMapping("/available-candidates")
    public ResponseEntity<ApiResponse<List<CandidateResponse>>>
    getAvailableCandidates() {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        true,
                        null,
                        service.getAvailableCandidates()
                )
        );
    }

    @Operation(summary = "Get Assessment by ID", description = "Fetches assessment details and assigned questions by assessment ID.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assessment details found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assessment not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssessmentResponse>> get(
            @Parameter(description = "Unique ID of the assessment", required = true) @PathVariable Long id) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        true,
                        null,
                        service.getById(id)
                )
        );
    }

    @Operation(summary = "Deactivate/Delete Assessment", description = "Deactivates an assessment by its ID.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assessment deactivated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assessment not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(
            @Parameter(description = "Unique ID of the assessment", required = true) @PathVariable Long id) {

        service.delete(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        true,
                        null,
                        "Assessment deactivated"
                )
        );
    }

    @Operation(summary = "Update Assessment Status", description = "Updates the lifecycle status of an assessment (e.g., PENDING, IN_PROGRESS, COMPLETED).")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status value"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assessment not found")
    })
    @PostMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>>
    changeStatus(
            @Parameter(description = "Unique ID of the assessment", required = true) @PathVariable Long id,
            @Parameter(description = "New status for the assessment", required = true) @RequestParam String status) {

        service.changeStatus(
                id,
                status
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        true,
                        null,
                        "Status updated successfully"
                )
        );
    }
}