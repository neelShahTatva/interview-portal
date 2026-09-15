package com.tatvasoft.interview_portal.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tatvasoft.interview_portal.ai.dto.EvaluationResult;
import com.tatvasoft.interview_portal.ai.dto.FileSubmissionRequest;
import com.tatvasoft.interview_portal.ai.service.AiProviderService;
import com.tatvasoft.interview_portal.constant.GeminiConstants;
import com.tatvasoft.interview_portal.entity.Question;
import com.tatvasoft.interview_portal.entity.QuestionSolution;
import com.tatvasoft.interview_portal.repository.QuestionSolutionRepository;
import com.tatvasoft.interview_portal.repository.QuestionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Service("geminiService")
public class GeminiEvaluationServiceImpl implements AiProviderService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private QuestionSolutionRepository questionSolutionRepository;
    @Autowired
    private QuestionsRepository questionsRepository;

    @Override
    public EvaluationResult evaluateCode(FileSubmissionRequest request) {
        try {

            if (request == null) {
                throw new IllegalArgumentException(
                        "Submission request is null"
                );
            }

            if (request.getQuestionId() == null) {
                throw new IllegalArgumentException(
                        "Question ID is required"
                );
            }

            if (request.getSubmissionFile() == null ||
                    request.getSubmissionFile().isEmpty()) {

                throw new IllegalArgumentException(
                        "Submission file is empty"
                );
            }

            Question question =
                    questionsRepository
                            .findById(request.getQuestionId())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Question not found"
                                    )
                            );

            QuestionSolution solution =
                    questionSolutionRepository
                            .findByQuestionIdAndIsActiveTrue(
                                    request.getQuestionId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Reference solution not found"
                                    )
                            );

            String candidateCode = new String(
                    request.getSubmissionFile().getBytes(), StandardCharsets.UTF_8
            );

            String referenceCode = solution.getSolutionCode();

            String promptText =
                    buildEvaluationPrompt(
                            question.getDescription(),
                            referenceCode,
                            candidateCode
                    );

            String requestBody = buildGeminiRequest(promptText, GeminiConstants.EVALUATION_SYSTEM_INSTRUCTION);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            String responseBody = callGeminiWithRetry(entity);

            String aiJson = extractAiText(responseBody);

            String cleanedJson = cleanAiJson(aiJson);

            EvaluationResult result =
                    objectMapper.readValue(
                            cleanedJson,
                            EvaluationResult.class
                    );

            validateEvaluationResult(result);

            return result;

        } catch (Exception e) {
            EvaluationResult error =
                    new EvaluationResult();

            error.setScore(0);

            error.setFeedback(
                    "We could not complete the code evaluation at this time. "
                            + "Please try again."
            );

            error.setTimeComplexity("N/A");
            error.setSpaceComplexity("N/A");

            return error;
        }
    }

    private String buildEvaluationPrompt(
            String question,
            String referenceCode,
            String candidateCode) {

        return GeminiConstants.EVALUATION_USER_PROMPT_TEMPLATE.formatted(
                question,
                referenceCode,
                candidateCode
        );
    }

    private String buildGeminiRequest(
            String promptText,
            String systemInstruction) throws Exception {

        ObjectNode root =
                objectMapper.createObjectNode();

        if (systemInstruction != null && !systemInstruction.isBlank()) {
            ObjectNode systemInstructionNode = root.putObject("system_instruction");
            ArrayNode sysParts = systemInstructionNode.putArray("parts");
            sysParts.addObject().put("text", systemInstruction);
        }

        ArrayNode contents =
                root.putArray("contents");

        ObjectNode content =
                contents.addObject();

        ArrayNode parts =
                content.putArray("parts");

        ObjectNode part =
                parts.addObject();

        part.put(
                "text",
                promptText
        );

        return objectMapper.writeValueAsString(root);
    }

    private String callGeminiWithRetry(
            HttpEntity<String> entity)
            throws InterruptedException {

        int maxRetries = 3;
        long waitTime = 2000;

        for (int attempt = 0;
             attempt < maxRetries;
             attempt++) {

            try {

                ResponseEntity<String> response =
                        restTemplate.postForEntity(
                                apiUrl + apiKey,
                                entity,
                                String.class
                        );

                if (!response.getStatusCode()
                        .is2xxSuccessful()) {

                    throw new RuntimeException(
                            "Gemini API returned HTTP "
                                    + response.getStatusCode().value()
                    );
                }

                if (response.getBody() == null ||
                        response.getBody().isBlank()) {

                    throw new RuntimeException(
                            "Gemini returned an empty response"
                    );
                }

                return response.getBody();

            } catch (HttpStatusCodeException e) {

                int statusCode =
                        e.getStatusCode().value();

                boolean retryable =
                        statusCode == 429 ||
                                statusCode == 500 ||
                                statusCode == 502 ||
                                statusCode == 503 ||
                                statusCode == 504;

                if (!retryable ||
                        attempt == maxRetries - 1) {

                    throw e;
                }
                Thread.sleep(waitTime);

                waitTime *= 2;
            }
        }

        throw new RuntimeException(
                "Gemini evaluation failed after retries"
        );
    }

    private String extractAiText(
            String responseBody) throws Exception {

        JsonNode root =
                objectMapper.readTree(responseBody);

        JsonNode candidates =
                root.path("candidates");

        if (!candidates.isArray() ||
                candidates.isEmpty()) {

            throw new RuntimeException(
                    "Gemini returned no candidates"
            );
        }

        JsonNode parts =
                candidates.get(0)
                        .path("content")
                        .path("parts");

        if (!parts.isArray() ||
                parts.isEmpty()) {

            throw new RuntimeException(
                    "Gemini returned no content"
            );
        }

        JsonNode textNode =
                parts.get(0).path("text");

        if (textNode.isMissingNode() ||
                textNode.asText().isBlank()) {

            throw new RuntimeException(
                    "Gemini returned empty evaluation"
            );
        }

        return textNode.asText();
    }

    private String cleanAiJson(
            String aiJson) {

        if (aiJson == null ||
                aiJson.isBlank()) {

            throw new RuntimeException(
                    "AI returned empty JSON"
            );
        }

        String cleaned =
                aiJson.trim();

        // Gemini sometimes returns ```json ... ```
        if (cleaned.startsWith("```json")) {

            cleaned =
                    cleaned.substring(
                            7
                    ).trim();

        } else if (cleaned.startsWith("```")) {

            cleaned =
                    cleaned.substring(
                            3
                    ).trim();
        }

        if (cleaned.endsWith("```")) {

            cleaned =
                    cleaned.substring(
                            0,
                            cleaned.length() - 3
                    ).trim();
        }

        int firstBrace =
                cleaned.indexOf('{');

        int lastBrace =
                cleaned.lastIndexOf('}');

        if (firstBrace >= 0 &&
                lastBrace > firstBrace) {

            cleaned =
                    cleaned.substring(
                            firstBrace,
                            lastBrace + 1
                    );
        }

        return cleaned.trim();
    }

    private void validateEvaluationResult(
            EvaluationResult result) {

        if (result == null) {
            throw new RuntimeException(
                    "Evaluation result is null"
            );
        }

        if (result.getScore() < 0 ||
                result.getScore() > 10) {

            throw new RuntimeException(
                    "Invalid score returned by Gemini"
            );
        }

        if (result.getFeedback() == null ||
                result.getFeedback().isBlank()) {

            throw new RuntimeException(
                    "Gemini returned empty feedback"
            );
        }

        if (result.getTimeComplexity() == null ||
                result.getTimeComplexity().isBlank()) {

            throw new RuntimeException(
                    "Time complexity is missing"
            );
        }

        if (result.getSpaceComplexity() == null ||
                result.getSpaceComplexity().isBlank()) {

            throw new RuntimeException(
                    "Space complexity is missing"
            );
        }

        if (result.getTimeComplexity().length() > 50) {
            throw new RuntimeException(
                    "Invalid time complexity"
            );
        }

        if (result.getSpaceComplexity().length() > 50) {
            throw new RuntimeException(
                    "Invalid space complexity"
            );
        }
    }

    @Override
    public String getProviderName() {
        return "gemini";
    }
}