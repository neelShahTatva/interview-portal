package com.tatvasoft.interview_portal.ai.service;

import com.tatvasoft.interview_portal.ai.dto.BulkQuestionCandidate;
import com.tatvasoft.interview_portal.ai.dto.QuestionSimilarityResult;
import com.tatvasoft.interview_portal.entity.Question;

import java.util.List;
import java.util.Map;

public interface AiQuestionSimilarityService {

    QuestionSimilarityResult isSimilarQuestion(
            String title,
            String description,
            List<Question> existingQuestions
    );

    default QuestionSimilarityResult checkQuestionSimilarity(
            String title,
            String description,
            List<Question> existingQuestions
    ) {
        return isSimilarQuestion(title, description, existingQuestions);
    }

    Map<Integer, QuestionSimilarityResult> checkBulkSimilarity(
            List<BulkQuestionCandidate> candidates,
            List<Question> existingQuestions
    );
}