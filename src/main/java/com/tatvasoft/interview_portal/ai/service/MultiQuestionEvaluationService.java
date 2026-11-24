package com.tatvasoft.interview_portal.ai.service;


import com.tatvasoft.interview_portal.dto.ai.FileSubmissionRequest;
import com.tatvasoft.interview_portal.dto.ai.MultiQuestionEvaluationResult;

import java.util.List;

public interface MultiQuestionEvaluationService {
    MultiQuestionEvaluationResult evaluateAll(List<FileSubmissionRequest> questions);
}