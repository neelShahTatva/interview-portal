package com.tatvasoft.interview_portal.ai.service;

import com.tatvasoft.interview_portal.dto.QuestionRecommendedResponse;
import com.tatvasoft.interview_portal.entity.Question;

import java.util.List;

public interface QuestionSelectionService {

    List<Long> selectQuestions(
            String designation,
            List<Question> questions,
            int maxMinutes);

}
