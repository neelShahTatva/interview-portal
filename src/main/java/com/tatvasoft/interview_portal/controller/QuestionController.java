package com.tatvasoft.interview_portal.controller;

import com.tatvasoft.interview_portal.dto.ApiResponse;
import com.tatvasoft.interview_portal.dto.CategoryResponse;
import com.tatvasoft.interview_portal.dto.QuestionRequest;
import com.tatvasoft.interview_portal.dto.QuestionResponse;
import com.tatvasoft.interview_portal.entity.Question;
import com.tatvasoft.interview_portal.service.QuestionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> addQuestion(
            @RequestBody QuestionRequest request) {

        questionService.addQuestion(request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        true,
                        null,
                        "Question Created successfully"
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getAll() {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        true,
                        null,
                        questionService.getAllQuestions()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionResponse>> get(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        true,
                        null,
                        questionService.getQuestion(id)
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> update(
            @PathVariable Long id,
            @RequestBody QuestionRequest request) {

        questionService.updateQuestion(id, request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        true,
                        null,
                        "Question updated successfully"
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(
            @PathVariable Long id) {

        questionService.deleteQuestion(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        true,
                        null,
                        "Question deleted successfully"
                )
        );
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<List<Question>>> upload(
            @RequestParam("file") MultipartFile file) {

        List<Question> uploadedQuestions =
                questionService.uploadExcel(file);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        true,
                        null,
                        uploadedQuestions
                )
        );
    }

    @GetMapping("/download-template")
    public ResponseEntity<Resource> downloadUploadTemplate() {
        Resource resource = new ClassPathResource("static/templates/Bulk_Questions_Template.xlsx");

        if (!resource.exists()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Bulk upload template not found."
            );
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"Bulk_Questions_Template.xlsx\"")
                .body(resource);
    }

    @GetMapping("/category")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        true,
                        null,
                        questionService.getAllCategories()
                )
        );
    }

    @GetMapping("/recommend")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> recommendQuestions(
            @RequestParam Long candidateId,
            @RequestParam(defaultValue = "90") Integer maxMinutes) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        true,
                        null,
                        questionService.recommendQuestions(
                                candidateId,
                                maxMinutes
                        )
                )
        );
    }
}