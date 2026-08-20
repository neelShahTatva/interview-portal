package com.tatvasoft.interview_portal.controller;

import com.tatvasoft.interview_portal.dto.ApiResponse;
import com.tatvasoft.interview_portal.dto.AssessmentRequest;
import com.tatvasoft.interview_portal.dto.AssessmentResponse;
import com.tatvasoft.interview_portal.dto.CandidateResponse;
import com.tatvasoft.interview_portal.service.AssessmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assessments")
public class AssessmentController {

    private final AssessmentService service;

    public AssessmentController(AssessmentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AssessmentResponse>> create(
            @RequestBody AssessmentRequest request) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        true,
                        null,
                        service.create(request)
                )
        );
    }

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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssessmentResponse>> get(
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

//    @PutMapping("/{id}")
//    public ResponseEntity<ApiResponse<AssessmentResponse>> update(
//            @PathVariable Long id,
//            @RequestBody AssessmentRequest request) {
//
//        return ResponseEntity.ok(
//                new ApiResponse<>(
//                        200,
//                        true,
//                        null,
//                        service.update(id, request)
//                )
//        );
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(
            @PathVariable Long id) {

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

    @PostMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>>
    changeStatus(
            @PathVariable Long id,
            @RequestParam String status) {

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