package com.tatvasoft.interview_portal.ai.service;


import com.tatvasoft.interview_portal.dto.ai.EvaluationResult;
import com.tatvasoft.interview_portal.dto.ai.FileSubmissionRequest;

public interface AiProviderService {
    EvaluationResult evaluateCode(FileSubmissionRequest submission);

    String getProviderName();
}
