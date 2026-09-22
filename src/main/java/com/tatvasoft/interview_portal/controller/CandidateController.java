package com.tatvasoft.interview_portal.controller;

import com.tatvasoft.interview_portal.dto.ApiResponse;
import com.tatvasoft.interview_portal.dto.CandidateEvaluationResponse;
import com.tatvasoft.interview_portal.dto.CandidateRequest;
import com.tatvasoft.interview_portal.dto.CandidateResponse;
import com.tatvasoft.interview_portal.service.CandidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/candidates")
@Tag(name = "Candidate Management", description = "Endpoints for managing candidates, candidate profiles, and evaluation summaries")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @Operation(summary = "Create Candidate", description = "Creates a new candidate profile in the system.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Candidate created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CandidateResponse>> create(@Valid @RequestBody CandidateRequest request) {

        CandidateResponse candidate = candidateService.create(request);

        return ResponseEntity.ok(new ApiResponse<>(200, true, null, candidate));
    }

    @Operation(summary = "Get All Candidates", description = "Retrieves a list of all candidates.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "List of candidates retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<CandidateResponse>>> getAll() {

        return ResponseEntity.ok(new ApiResponse<>(200, true, null, candidateService.getAll()));
    }

    @Operation(summary = "Get Candidate by ID", description = "Fetches details of a specific candidate by their candidate ID.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Candidate found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Candidate not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CandidateResponse>> getById(
            @Parameter(description = "Unique ID of the candidate", required = true) @PathVariable Long id) {

        return ResponseEntity.ok(new ApiResponse<>(200, true, null, candidateService.getById(id)));
    }

    @Operation(summary = "Update Candidate", description = "Updates candidate profile details.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Candidate updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Candidate not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CandidateResponse>> update(
            @Parameter(description = "Unique ID of the candidate to update", required = true) @PathVariable Long id,
            @Valid @RequestBody CandidateRequest request) {

        CandidateResponse updatedCandidate = candidateService.update(id, request);

        return ResponseEntity.ok(new ApiResponse<>(200, true, null, updatedCandidate));
    }

    @Operation(summary = "Delete Candidate", description = "Deletes a candidate by their candidate ID.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Candidate deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Candidate not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(
            @Parameter(description = "Unique ID of the candidate to delete", required = true) @PathVariable Long id) {

        candidateService.delete(id);

        return ResponseEntity.ok(new ApiResponse<>(200, true, null, "Candidate deleted successfully"));
    }

    @Operation(summary = "Get Candidate Evaluation Summary", description = "Retrieves full evaluation and question results for a candidate's assessment.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Evaluation details retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Candidate evaluation not found")
    })
    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<CandidateEvaluationResponse> getCandidateEvaluation(
            @Parameter(description = "Unique ID of the candidate", required = true) @PathVariable Long candidateId) {

        return ResponseEntity.ok(candidateService.getCandidateEvaluation(candidateId));
    }
}