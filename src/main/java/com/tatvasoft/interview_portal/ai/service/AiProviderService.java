package com.tatvasoft.interview_portal.ai.service;


import com.tatvasoft.interview_portal.ai.dto.EvaluationResult;
import com.tatvasoft.interview_portal.ai.dto.FileSubmissionRequest;

public interface AiProviderService {
    EvaluationResult evaluateCode(FileSubmissionRequest submission);

    String getProviderName();
}
