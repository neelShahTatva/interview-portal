package com.tatvasoft.interview_portal.util;

import com.tatvasoft.interview_portal.ai.dto.EvaluationResult;
import com.tatvasoft.interview_portal.ai.dto.FileSubmissionRequest;
import org.springframework.stereotype.Component;

@Component
public class EvaluationValidationUtil {

    public void validateRequest(FileSubmissionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Submission request is null"
            );
        }

        if (request.getQuestionId() == null) {
            throw new IllegalArgumentException(
                    "Question ID is required"
            );
        }

        if (request.getSubmissionFile() == null ||
                request.getSubmissionFile().isEmpty()) {

            throw new IllegalArgumentException(
                    "Submission file is empty"
            );
        }
    }

    public void validateEvaluationResult(EvaluationResult result) {
        if (result == null) {
            throw new RuntimeException(
                    "Evaluation result is null"
            );
        }

        if (result.getScore() < 0 || result.getScore() > 10) {
            throw new RuntimeException(
                    "Invalid score returned by AI provider"
            );
        }

        if (result.getFeedback() == null || result.getFeedback().isBlank()) {
            throw new RuntimeException(
                    "AI provider returned empty feedback"
            );
        }

        if (result.getTimeComplexity() == null || result.getTimeComplexity().isBlank()) {
            throw new RuntimeException(
                    "Time complexity is missing"
            );
        }

        if (result.getSpaceComplexity() == null || result.getSpaceComplexity().isBlank()) {
            throw new RuntimeException(
                    "Space complexity is missing"
            );
        }

        if (result.getTimeComplexity().length() > 50) {
            throw new RuntimeException(
                    "Invalid time complexity"
            );
        }

        if (result.getSpaceComplexity().length() > 50) {
            throw new RuntimeException(
                    "Invalid space complexity"
            );
        }
    }
}
