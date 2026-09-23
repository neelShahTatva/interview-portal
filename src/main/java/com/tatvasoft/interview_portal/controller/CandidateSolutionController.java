package com.tatvasoft.interview_portal.controller;


import com.tatvasoft.interview_portal.dto.ApiResponse;
import com.tatvasoft.interview_portal.dto.CandidateSolutionResponse;
import com.tatvasoft.interview_portal.service.CandidateSolutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/candidate-solutions")
@Tag(name = "Candidate Solutions", description = "Endpoints for retrieving candidate question solutions and AI scores by submission")
public class CandidateSolutionController {

    private final CandidateSolutionService service;

    public CandidateSolutionController(
            CandidateSolutionService service) {

        this.service = service;
    }

    @Operation(summary = "Get Solutions by Submission ID", description = "Retrieves all question solutions, AI scores, and AI feedback for a specific submission.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Solutions retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid submission ID supplied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Submission not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{submissionId}")
    public ResponseEntity<
            ApiResponse<List<CandidateSolutionResponse>>>
    getBySubmissionId(
            @Parameter(description = "Unique ID of the submission", required = true)
            @PathVariable Long submissionId) {

        return ResponseEntity.ok(

                new ApiResponse<>(
                        200,
                        true,
                        null,
                        service.getBySubmissionId(
                                submissionId)
                )
        );
    }
}
