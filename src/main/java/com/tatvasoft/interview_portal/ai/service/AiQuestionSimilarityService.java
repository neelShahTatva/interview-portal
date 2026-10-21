package com.tatvasoft.interview_portal.ai.service;

import com.tatvasoft.interview_portal.entity.Question;

import java.util.List;

public interface AiQuestionSimilarityService {

    boolean isSimilarQuestion(
            String title,
            String description,
            List<Question> existingQuestions
    );
}
