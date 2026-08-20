package com.tatvasoft.interview_portal.ai.controller;

import com.tatvasoft.interview_portal.ai.dto.EvaluationResult;
import com.tatvasoft.interview_portal.ai.dto.FileSubmissionRequest;
import com.tatvasoft.interview_portal.ai.dto.MultiQuestionEvaluationResult;
import com.tatvasoft.interview_portal.ai.service.MultiQuestionEvaluationService;
import com.tatvasoft.interview_portal.ai.service.router.EvaluationRouter;
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
public class InterviewController {

    @Autowired
    private EvaluationRouter evaluationRouter;

    @Autowired
    private MultiQuestionEvaluationService multiQuestionEvaluationService;

    // ─── Single Question ────────────────────────────────────────────
    @PostMapping(value = "/evaluate-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EvaluationResult> evaluateSingle(
            @RequestParam Long questionId,
            @RequestParam Long assessmentId,
            @RequestParam Long candidateId,

            @RequestPart("submission")
            MultipartFile submissionFile) {
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
    @PostMapping(value = "/evaluate-multi", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MultiQuestionEvaluationResult> evaluateMultiple(
            @RequestParam("assessmentId") Long assessmentId,

            @RequestParam("candidateId") Long candidateId,

            @RequestParam("totalQuestions") int totalQuestions,

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