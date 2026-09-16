package com.tatvasoft.interview_portal.util;

import com.tatvasoft.interview_portal.constant.GeminiConstants;
import org.springframework.stereotype.Component;

@Component
public class AiEvaluationUtil {

    public String buildEvaluationPrompt(
            String question,
            String referenceCode,
            String candidateCode) {

        return GeminiConstants.EVALUATION_USER_PROMPT_TEMPLATE.formatted(
                question != null ? question : "",
                referenceCode != null ? referenceCode : "",
                candidateCode != null ? candidateCode : ""
        );
    }

    public String cleanAiJson(String aiJson) {
        if (aiJson == null || aiJson.isBlank()) {
            throw new RuntimeException("AI returned empty JSON");
        }

        String cleaned = aiJson.trim();

        // Strip markdown code fences if present (e.g. ```json ... ```)
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7).trim();
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3).trim();
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
        }

        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');

        if (firstBrace >= 0 && lastBrace > firstBrace) {
            cleaned = cleaned.substring(firstBrace, lastBrace + 1);
        }

        return cleaned.trim();
    }
}
