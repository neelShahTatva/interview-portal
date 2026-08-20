package com.tatvasoft.interview_portal.controller;

import com.tatvasoft.interview_portal.dto.ApiResponse;
import com.tatvasoft.interview_portal.dto.SubmissionRequest;
import com.tatvasoft.interview_portal.dto.SubmissionResponse;
import com.tatvasoft.interview_portal.service.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/submissions")
public class SubmissionController {

    private final SubmissionService service;

    public SubmissionController(SubmissionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SubmissionResponse>> create(
            @RequestBody SubmissionRequest request) {

        SubmissionResponse submission = service.create(request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        true,
                        null,
                        submission
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getAll() {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        true,
                        null,
                        service.getAll()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubmissionResponse>> get(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        true,
                        null,
                        service.getById(id)
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubmissionResponse>> update(
            @PathVariable Long id,
            @RequestBody SubmissionRequest request) {

        SubmissionResponse updatedSubmission =
                service.update(id, request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        true,
                        null,
                        updatedSubmission
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(
            @PathVariable Long id) {

        service.delete(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        true,
                        null,
                        "Submission deleted successfully"
                )
        );
    }
}