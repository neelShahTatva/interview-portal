package com.tatvasoft.interview_portal.ai.controller;

import com.tatvasoft.interview_portal.ai.dto.EvaluationResult;
import com.tatvasoft.interview_portal.ai.dto.FileSubmissionRequest;
import com.tatvasoft.interview_portal.ai.dto.MultiQuestionEvaluationResult;
import com.tatvasoft.interview_portal.ai.service.MultiQuestionEvaluationService;
import com.tatvasoft.interview_portal.ai.service.router.EvaluationRouter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/mock-interview")
@CrossOrigin(origins = "*") // Allows testing from anywhere
@Tag(name = "AI Interview Evaluation", description = "Endpoints for automated single-file and multi-question code evaluation via AI (Gemini/Groq)")
public class InterviewController {

    @Autowired
    private EvaluationRouter evaluationRouter;

    @Autowired
    private MultiQuestionEvaluationService multiQuestionEvaluationService;

    // ─── Single Question ────────────────────────────────────────────
    @Operation(summary = "Evaluate Single Question Submission", description = "Evaluates candidate code submission against problem statement and reference solution using AI.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evaluation completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or missing file"),
            @ApiResponse(responseCode = "500", description = "Internal server error during AI evaluation")
    })
    @PostMapping(value = "/evaluate-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EvaluationResult> evaluateSingle(
            @Parameter(description = "Question ID", required = true) @RequestParam Long questionId,
            @Parameter(description = "Assessment ID", required = true) @RequestParam Long assessmentId,
            @Parameter(description = "Candidate ID", required = true) @RequestParam Long candidateId,
            @Parameter(description = "Code file submitted by candidate", required = true)
            @RequestPart("submission") MultipartFile submissionFile) {
        FileSubmissionRequest request =
                new FileSubmissionRequest();

        request.setQuestionId(questionId);
        request.setAssessmentId(assessmentId);
        request.setCandidateId(candidateId);
        request.setSubmissionFile(submissionFile);

        EvaluationResult result =
                evaluationRouter.routeEvaluation(request);

        return ResponseEntity.ok(result);
    }

    // ─── Multiple Questions ─────────────────────────────────────────
    @Operation(summary = "Evaluate Multiple Questions", description = "Evaluates all question submissions in a candidate assessment in batch.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Multi-question evaluation completed"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters or missing question submissions"),
            @ApiResponse(responseCode = "500", description = "Internal server error during batch evaluation")
    })
    @PostMapping(value = "/evaluate-multi", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MultiQuestionEvaluationResult> evaluateMultiple(
            @Parameter(description = "Assessment ID", required = true) @RequestParam("assessmentId") Long assessmentId,
            @Parameter(description = "Candidate ID", required = true) @RequestParam("candidateId") Long candidateId,
            @Parameter(description = "Total number of questions in submission", required = true) @RequestParam("totalQuestions") int totalQuestions,
            HttpServletRequest httpRequest) {

        MultipartHttpServletRequest multipartRequest =
                (MultipartHttpServletRequest) httpRequest;

        List<FileSubmissionRequest> questions = new ArrayList<>();

        for (int i = 0; i < totalQuestions; i++) {
            Long questionId =
                    Long.valueOf(
                            multipartRequest.getParameter(
                                    "questionId_" + i
                            )
                    );
            MultipartFile submission =
                    multipartRequest.getFile(
                            "submission_" + i
                    );

            if (questionId  == null || submission == null) {
                return ResponseEntity.badRequest().build();
            }

            FileSubmissionRequest req =
                    new FileSubmissionRequest();
            req.setQuestionId(questionId);
            req.setSubmissionFile(submission);
            req.setAssessmentId(assessmentId);
            req.setCandidateId(candidateId);
            questions.add(req);
        }

        MultiQuestionEvaluationResult result =
                multiQuestionEvaluationService.evaluateAll(questions);

        return ResponseEntity.ok(result);
    }
}