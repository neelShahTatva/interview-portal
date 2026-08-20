package com.tatvasoft.interview_portal.ai.service;

import com.tatvasoft.interview_portal.entity.QuestionSolution;

public interface AiSolutionGenerationService {

    QuestionSolution generateSolution(
            String title,
            String description
    );
}