package com.tatvasoft.interview_portal.ai.service.router;

import com.tatvasoft.interview_portal.ai.dto.EvaluationResult;
import com.tatvasoft.interview_portal.ai.dto.FileSubmissionRequest;
import com.tatvasoft.interview_portal.ai.service.AiProviderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EvaluationRouter {
    private final Map<String, AiProviderService> services;
    private final String activeProvider;

    // Spring automatically injects a List of ALL classes that implement AiEvaluationService
    public EvaluationRouter(
            List<AiProviderService> serviceList,
            @Value("${ai.provider.active}") String activeProvider) {

        this.activeProvider = activeProvider.toLowerCase();

        // Convert the list into a Map so we can look them up by name (e.g., "gemini" -> GeminiEvaluationServiceImpl)
        this.services = serviceList.stream()
                .collect(Collectors.toMap(AiProviderService::getProviderName, service -> service));
    }

    public EvaluationResult routeEvaluation(FileSubmissionRequest submission) {
        AiProviderService selectedService = services.get(activeProvider);

        if (selectedService == null) {
            EvaluationResult error = new EvaluationResult();
            error.setFeedback("Configuration Error: Active AI provider '" + activeProvider + "' not found.");
            return error;
        }
        System.out.println(activeProvider);
        // Execute the evaluation using the chosen AI
        return selectedService.evaluateCode(submission);
    }
}
