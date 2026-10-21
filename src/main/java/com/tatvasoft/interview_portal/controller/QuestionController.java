package com.tatvasoft.interview_portal.controller;

import com.tatvasoft.interview_portal.dto.CategoryResponse;
import com.tatvasoft.interview_portal.dto.QuestionRequest;
import com.tatvasoft.interview_portal.dto.QuestionResponse;
import com.tatvasoft.interview_portal.entity.Question;
import com.tatvasoft.interview_portal.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/questions")
@Tag(name = "Question Management", description = "Endpoints for managing question bank, categories, bulk Excel upload, template download, and AI question recommendations")
@Validated
public class QuestionController {

    private final QuestionService questionService;

    @Value("${bulk.question.template.path}")
    private String bulkQuestionTemplatePath;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @Operation(summary = "Add New Question", description = "Creates a new question with optional AI-generated solutions and category associations.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Question created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid question data or validation failure"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<String>> addQuestion(
            @RequestBody QuestionRequest request) {

        questionService.addQuestion(request);

        return ResponseEntity.ok(
                new com.tatvasoft.interview_portal.dto.ApiResponse<>(
                        200,
                        true,
                        null,
                        "Question Created successfully"
                )
        );
    }

    @Operation(summary = "Get All Questions", description = "Retrieves all questions from the question bank.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Questions retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<List<QuestionResponse>>> getAll() {

        return ResponseEntity.ok(
                new com.tatvasoft.interview_portal.dto.ApiResponse<>(
                        200,
                        true,
                        null,
                        questionService.getAllQuestions()
                )
        );
    }

    @Operation(summary = "Get Question by ID", description = "Fetches a single question along with its categories and solutions.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Question found"),
            @ApiResponse(responseCode = "400", description = "Invalid question ID supplied"),
            @ApiResponse(responseCode = "404", description = "Question not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<QuestionResponse>> get(
            @Parameter(description = "Unique ID of the question", required = true) @PathVariable Long id) {

        return ResponseEntity.ok(
                new com.tatvasoft.interview_portal.dto.ApiResponse<>(
                        200,
                        true,
                        null,
                        questionService.getQuestion(id)
                )
        );
    }

    @Operation(summary = "Update Question", description = "Updates an existing question's details, categories, or solutions.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Question updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid update payload or validation failure"),
            @ApiResponse(responseCode = "404", description = "Question not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<String>> update(
            @Parameter(description = "Unique ID of the question to update", required = true) @PathVariable Long id,
            @RequestBody QuestionRequest request) {

        questionService.updateQuestion(id, request);

        return ResponseEntity.ok(
                new com.tatvasoft.interview_portal.dto.ApiResponse<>(
                        200,
                        true,
                        null,
                        "Question updated successfully"
                )
        );
    }

    @Operation(summary = "Delete Question", description = "Deletes a question from the repository.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Question deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid question ID supplied"),
            @ApiResponse(responseCode = "404", description = "Question not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<String>> delete(
            @Parameter(description = "Unique ID of the question to delete", required = true) @PathVariable Long id) {

        questionService.deleteQuestion(id);

        return ResponseEntity.ok(
                new com.tatvasoft.interview_portal.dto.ApiResponse<>(
                        200,
                        true,
                        null,
                        "Question deleted successfully"
                )
        );
    }

    @Operation(summary = "Bulk Upload Questions via Excel", description = "Uploads questions in bulk from an Excel file (.xlsx / .xls).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Questions uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or corrupted Excel file"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<List<Question>>> upload(
            @Parameter(description = "Excel file (.xlsx / .xls) containing questions", required = true)
            @RequestParam("file") MultipartFile file) {

        List<Question> uploadedQuestions =
                questionService.uploadExcel(file);

        return ResponseEntity.ok(
                new com.tatvasoft.interview_portal.dto.ApiResponse<>(
                        200,
                        true,
                        null,
                        uploadedQuestions
                )
        );
    }

    @Operation(summary = "Download Bulk Upload Template", description = "Downloads the Excel template used for bulk question imports.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template file downloaded"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "404", description = "Template file not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/download-template")
    public ResponseEntity<Resource> downloadUploadTemplate() {
       Resource resource = new ClassPathResource(bulkQuestionTemplatePath);
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

    @Operation(summary = "Get All Categories", description = "Retrieves all question categories available in the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/category")
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<List<CategoryResponse>>> getAllCategories() {

        return ResponseEntity.ok(
                new com.tatvasoft.interview_portal.dto.ApiResponse<>(
                        200,
                        true,
                        null,
                        questionService.getAllCategories()
                )
        );
    }

    @Operation(summary = "Recommend Questions for Candidate", description = "Uses AI recommendation to select questions suited to candidate experience and designation within a max time limit.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recommended questions retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid candidate ID or maxMinutes constraints"),
            @ApiResponse(responseCode = "404", description = "Candidate not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/recommend")
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<List<QuestionResponse>>> recommendQuestions(
            @Parameter(description = "Candidate ID for tailored recommendation", required = true) @RequestParam Long candidateId,
            @Parameter(description = "Maximum duration in minutes", example = "90") @RequestParam(defaultValue = "90")
            @Min(value = 30, message = "Assessment time must be at least 30 minutes")
            @Max(value = 180, message = "Assessment time must not exceed 180 minutes")
            Integer maxMinutes) {

        return ResponseEntity.ok(
                new com.tatvasoft.interview_portal.dto.ApiResponse<>(
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