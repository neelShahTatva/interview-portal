package com.tatvasoft.interview_portal.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tatvasoft.interview_portal.ai.service.AiQuestionSimilarityService;
import com.tatvasoft.interview_portal.entity.Question;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
public class GeminiQuestionSimilarityServiceImpl implements AiQuestionSimilarityService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean isSimilarQuestion(String title, String description, List<Question> existingQuestions) {
        if (StringUtils.isBlank(title) || existingQuestions == null || existingQuestions.isEmpty()) {
            return false;
        }

        String trimmedTitle = title.trim();
        String trimmedDesc = description != null ? description.trim() : "";

        // 1. Exact match check (title or description)
        for (Question existing : existingQuestions) {
            if (existing == null) {
                continue;
            }

            String existingTitle = existing.getTitle() != null ? existing.getTitle().trim() : "";
            String existingDesc = existing.getDescription() != null ? existing.getDescription().trim() : "";

            if (existingTitle.equalsIgnoreCase(trimmedTitle)) {
                log.info("Exact duplicate question title found with Question ID: {}", existing.getId());
                return true;
            }

            if (!trimmedDesc.isEmpty() && !existingDesc.isEmpty() && existingDesc.equalsIgnoreCase(trimmedDesc)) {
                log.info("Exact duplicate question description found with Question ID: {}", existing.getId());
                return true;
            }
        }

        // 2. AI semantic similarity check
        try {
            return checkSemanticSimilarityWithGemini(trimmedTitle, trimmedDesc, existingQuestions);
        } catch (Exception e) {
            log.error("Failed to check question similarity with Gemini AI: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean checkSemanticSimilarityWithGemini(String title, String description, List<Question> existingQuestions) throws Exception {
        StringBuilder questionsList = new StringBuilder();
        for (Question q : existingQuestions) {
            if (q == null) {
                continue;
            }
            questionsList.append("- ID: ").append(q.getId()).append("\n")
                    .append("  Title: ").append(q.getTitle() != null ? q.getTitle().replace("\"", "'") : "").append("\n")
                    .append("  Description: ").append(q.getDescription() != null ? q.getDescription().replace("\"", "'") : "").append("\n");
        }

        String prompt = String.format("""
                You are an AI assistant helping an interview portal detect duplicate or semantically similar technical interview questions.
                
                A question is considered similar/duplicate if it tests the exact same algorithmic problem, coding logic, or conceptual scenario (even if written with different problem stories, variable names, rephrased descriptions, or slight wording modifications).
                
                Candidate Question:
                Title: %s
                Description: %s
                
                Existing Active Questions in Database:
                %s
                
                Instructions:
                1. Carefully compare the Candidate Question against each Existing Question.
                2. If the Candidate Question is semantically identical or essentially the same problem as any existing question, set "isSimilar": true and provide the matched question ID.
                3. If the Candidate Question is unique, set "isSimilar": false.
                4. Return ONLY valid JSON in the format:
                {
                  "isSimilar": true,
                  "matchedQuestionId": 123,
                  "reason": "Brief reason"
                }
                Do not include markdown or explanations outside the JSON.
                """,
                title.replace("\"", "'"),
                description.replace("\"", "'"),
                questionsList
        );

        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode contents = root.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(root.toString(), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                apiUrl + apiKey,
                entity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            log.warn("Gemini API returned non-2xx status: {}", response.getStatusCode());
            return false;
        }

        JsonNode responseRoot = objectMapper.readTree(response.getBody());
        JsonNode candidates = responseRoot.path("candidates");
        if (candidates.isEmpty()) {
            return false;
        }

        String responseText = candidates.get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();

        String cleanedJson = cleanJson(responseText);
        JsonNode resultNode = objectMapper.readTree(cleanedJson);
        boolean isSimilar = resultNode.path("isSimilar").asBoolean(false);

        if (isSimilar) {
            log.info("AI detected similar question. Matched ID: {}, Reason: {}",
                    resultNode.path("matchedQuestionId").asText(),
                    resultNode.path("reason").asText());
        }

        return isSimilar;
    }

    private String cleanJson(String rawText) {
        if (rawText == null) {
            return "{}";
        }
        String cleaned = rawText.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }
}