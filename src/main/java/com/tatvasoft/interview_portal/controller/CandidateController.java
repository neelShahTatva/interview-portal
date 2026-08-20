package com.tatvasoft.interview_portal.controller;

import com.tatvasoft.interview_portal.dto.ApiResponse;
import com.tatvasoft.interview_portal.dto.CandidateEvaluationResponse;
import com.tatvasoft.interview_portal.dto.CandidateRequest;
import com.tatvasoft.interview_portal.dto.CandidateResponse;
import com.tatvasoft.interview_portal.dto.CandidateSolutionResponse;
import com.tatvasoft.interview_portal.service.CandidateService;
import com.tatvasoft.interview_portal.service.CandidateSolutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    // CREATE CANDIDATE
    @PostMapping
    public ResponseEntity<ApiResponse<CandidateResponse>> create(@RequestBody CandidateRequest request) {

        CandidateResponse candidate = candidateService.create(request);

        return ResponseEntity.ok(new ApiResponse<>(200, true, null, candidate));
    }

    // GET ALL CANDIDATES
    @GetMapping
    public ResponseEntity<ApiResponse<List<CandidateResponse>>> getAll() {

        return ResponseEntity.ok(new ApiResponse<>(200, true, null, candidateService.getAll()));
    }

    // GET CANDIDATE BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CandidateResponse>> getById(@PathVariable Long id) {

        return ResponseEntity.ok(new ApiResponse<>(200, true, null, candidateService.getById(id)));
    }

    // UPDATE CANDIDATE
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CandidateResponse>> update(@PathVariable Long id, @RequestBody CandidateRequest request) {

        CandidateResponse updatedCandidate = candidateService.update(id, request);

        return ResponseEntity.ok(new ApiResponse<>(200, true, null, updatedCandidate));
    }

    // DELETE CANDIDATE
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {

        candidateService.delete(id);

        return ResponseEntity.ok(new ApiResponse<>(200, true, null, "Candidate deleted successfully"));
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<CandidateEvaluationResponse> getCandidateEvaluation(@PathVariable Long candidateId) {

        return ResponseEntity.ok(candidateService.getCandidateEvaluation(candidateId));
    }
}