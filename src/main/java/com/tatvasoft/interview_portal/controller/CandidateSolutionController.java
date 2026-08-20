package com.tatvasoft.interview_portal.controller;


import com.tatvasoft.interview_portal.dto.ApiResponse;
import com.tatvasoft.interview_portal.dto.CandidateSolutionResponse;
import com.tatvasoft.interview_portal.service.CandidateSolutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/candidate-solutions")
public class CandidateSolutionController {

    private final CandidateSolutionService service;

    public CandidateSolutionController(
            CandidateSolutionService service) {

        this.service = service;
    }

    @GetMapping("/{submissionId}")
    public ResponseEntity<
            ApiResponse<List<CandidateSolutionResponse>>>
    getBySubmissionId(
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
