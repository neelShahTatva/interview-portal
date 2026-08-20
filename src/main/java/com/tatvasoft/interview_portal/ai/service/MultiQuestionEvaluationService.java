package com.tatvasoft.interview_portal.ai.service;


import com.tatvasoft.interview_portal.ai.dto.FileSubmissionRequest;
import com.tatvasoft.interview_portal.ai.dto.MultiQuestionEvaluationResult;

import java.util.List;

public interface MultiQuestionEvaluationService {
    MultiQuestionEvaluationResult evaluateAll(List<FileSubmissionRequest> questions);
}