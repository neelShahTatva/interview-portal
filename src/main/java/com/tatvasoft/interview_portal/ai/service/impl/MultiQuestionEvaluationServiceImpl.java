package com.tatvasoft.interview_portal.ai.service.impl;

import com.tatvasoft.interview_portal.ai.dto.EvaluationResult;
import com.tatvasoft.interview_portal.ai.dto.FileSubmissionRequest;
import com.tatvasoft.interview_portal.ai.dto.MultiQuestionEvaluationResult;
import com.tatvasoft.interview_portal.ai.dto.QuestionEvaluationResult;
import com.tatvasoft.interview_portal.ai.service.MultiQuestionEvaluationService;
import com.tatvasoft.interview_portal.ai.service.router.EvaluationRouter;
import com.tatvasoft.interview_portal.entity.Assessment;
import com.tatvasoft.interview_portal.entity.CandidateSolution;
import com.tatvasoft.interview_portal.entity.Submission;
import com.tatvasoft.interview_portal.enums.AssessmentStatus;
import com.tatvasoft.interview_portal.repository.AssessmentRepository;
import com.tatvasoft.interview_portal.repository.CandidateSolutionRepository;
import com.tatvasoft.interview_portal.repository.QuestionsRepository;
import com.tatvasoft.interview_portal.repository.SubmissionRepository;
import com.tatvasoft.interview_portal.util.JwtUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class MultiQuestionEvaluationServiceImpl implements MultiQuestionEvaluationService {

    private final EvaluationRouter evaluationRouter;
    private final QuestionsRepository questionsRepository;
    private final SubmissionRepository submissionRepository;
    private final CandidateSolutionRepository candidateSolutionRepository;
    private final AssessmentRepository assessmentRepository;

    public MultiQuestionEvaluationServiceImpl(EvaluationRouter evaluationRouter, QuestionsRepository questionsRepository, SubmissionRepository submissionRepository, CandidateSolutionRepository candidateSolutionRepository, AssessmentRepository assessmentRepository) {
        this.evaluationRouter = evaluationRouter;
        this.questionsRepository = questionsRepository;
        this.submissionRepository = submissionRepository;
        this.candidateSolutionRepository = candidateSolutionRepository;
        this.assessmentRepository = assessmentRepository;
    }

    @Override
    @Transactional
    public MultiQuestionEvaluationResult evaluateAll(List<FileSubmissionRequest> questions) {
        FileSubmissionRequest firstQuestion = questions.get(0);
        Long userId =
                (Long) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getDetails();
        Submission submission = new Submission();
        submission.setAssessmentId(firstQuestion.getAssessmentId());
        submission.setCandidateId(firstQuestion.getCandidateId());
        submission.setEvaluatedAt(LocalDateTime.now());
        submission.setCreatedAt(LocalDateTime.now());
        submission.setCreatedBy(userId);

        submission = submissionRepository.save(submission);
        ExecutorService executor = Executors.newFixedThreadPool(questions.size());
        List<Future<QuestionEvaluationResult>> futures = new ArrayList<>();

        // Submit all questions in parallel to whichever AI is active
        for (int i = 0; i < questions.size(); i++) {
            final int index = i;
            final FileSubmissionRequest request = questions.get(i);

            futures.add(executor.submit(() -> {
                QuestionEvaluationResult qResult = new QuestionEvaluationResult();
                String candidateCode = new String(request.getSubmissionFile().getBytes(), StandardCharsets.UTF_8);
                qResult.setQuestionNumber(index + 1);
                String questionTitle = questionsRepository.findById(request.getQuestionId()).map(q -> q.getDescription()).orElse("Unknown Question");
                qResult.setQuestionTopic(questionTitle);
                EvaluationResult evaluation = evaluationRouter.routeEvaluation(request);
                qResult.setQuestionId(request.getQuestionId());
                qResult.setCandidateCode(candidateCode);
                qResult.setScore(evaluation.getScore());
                qResult.setFeedback(evaluation.getFeedback());
                qResult.setTimeComplexity(evaluation.getTimeComplexity());
                qResult.setSpaceComplexity(evaluation.getSpaceComplexity());
                qResult.setMissedEdgeCases(evaluation.getMissedEdgeCases());
                qResult.setSecurityIssues(evaluation.getSecurityIssues());
                qResult.setOptimizedCode(evaluation.getOptimizedCode());
                return qResult;
            }));
        }

        executor.shutdown();

        // Collect results in original order
        List<QuestionEvaluationResult> results = new ArrayList<>();

        for (Future<QuestionEvaluationResult> future : futures) {
            try {
                results.add(future.get(120, TimeUnit.SECONDS));
            } catch (TimeoutException e) {
                results.add(buildErrorResult("Evaluation timed out.", 0));
            } catch (Exception e) {
                results.add(buildErrorResult("Evaluation failed: " + e.getMessage(), 0));
            }
        }
        for (QuestionEvaluationResult result : results) {

            CandidateSolution solution =
                    new CandidateSolution();

            solution.setSubmissionId(submission.getId());
            solution.setQuestionId(result.getQuestionId());
            solution.setAiScore(result.getScore());
            solution.setAiFeedback(result.getFeedback());
            solution.setTimeComplexity(result.getTimeComplexity());
            solution.setSpaceComplexity(result.getSpaceComplexity());
            solution.setMissedEdgeCases(result.getMissedEdgeCases().toString());
            solution.setSecurityIssues(result.getSecurityIssues().toString());
            solution.setOptimizedCode(result.getOptimizedCode());
            solution.setSolution(result.getCandidateCode());
            candidateSolutionRepository.save(solution);
        }
        double overallScore =
                results.stream()
                        .mapToInt(
                                QuestionEvaluationResult::getScore
                        )
                        .average()
                        .orElse(0);
        submission.setAiScore((int) overallScore);
        submission.setOutput(
                overallScore >= 7
                        ? "PASS"
                        : "FAIL"
        );
        submissionRepository.save(submission);
        Assessment assessment =
                assessmentRepository.findById(
                        firstQuestion.getAssessmentId()
                ).orElseThrow(() -> new RuntimeException("Assessment not found"));

        assessment.setStatus(AssessmentStatus.COMPLETED.toString());
        assessment.setCompletedAt(LocalDateTime.now());
        assessment.setUpdatedAt(LocalDateTime.now());
        assessment.setUpdatedBy(userId);
        assessmentRepository.save(assessment);
        MultiQuestionEvaluationResult finalResult = new MultiQuestionEvaluationResult();
        finalResult.setTotalQuestions(results.size());
        finalResult.setOverallScore(overallScore);
        finalResult.setEvaluations(results);

        finalResult.setIsSuccess(true);

        return finalResult;
    }

    private QuestionEvaluationResult buildErrorResult(String message, int score) {
        QuestionEvaluationResult qr =
                new QuestionEvaluationResult();

        qr.setScore(score);
        qr.setFeedback(message);

        return qr;
    }
}