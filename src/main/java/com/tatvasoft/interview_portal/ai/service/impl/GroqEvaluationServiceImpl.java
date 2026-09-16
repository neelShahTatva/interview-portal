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
import com.tatvasoft.interview_portal.util.AiEvaluationUtil;
import com.tatvasoft.interview_portal.util.EvaluationValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Service("groqService")
public class GroqEvaluationServiceImpl implements AiProviderService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.model}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private QuestionSolutionRepository
            questionSolutionRepository;
    @Autowired
    private QuestionsRepository questionsRepository;
    @Autowired
    private EvaluationValidationUtil evaluationValidationUtil;
    @Autowired
    private AiEvaluationUtil aiEvaluationUtil;

    @Override
    public EvaluationResult evaluateCode(FileSubmissionRequest request) {
        try {
            evaluationValidationUtil.validateRequest(request);

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
            Question question =
                    questionsRepository
                            .findById(request.getQuestionId())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Question not found"
                                    )
                            );
            String solutionCode =
                    solution.getSolutionCode();
            String candidateCode = new String(
                    request.getSubmissionFile().getBytes(), StandardCharsets.UTF_8
            );

            String systemInstruction = GeminiConstants.EVALUATION_SYSTEM_INSTRUCTION;

            String userPrompt = aiEvaluationUtil.buildEvaluationPrompt(
                    question.getDescription(),
                    solutionCode,
                    candidateCode
            );

            // Build request body using Jackson (avoids manual JSON escaping)
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", modelName);

            // Force JSON output mode (supported by Groq)
            ObjectNode responseFormat = objectMapper.createObjectNode();
            responseFormat.put("type", "json_object");
            requestBody.set("response_format", responseFormat);

            ArrayNode messages = requestBody.putArray("messages");

            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemInstruction);
            messages.add(systemMessage);

            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt);
            messages.add(userMessage);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    apiUrl, requestEntity, String.class
            );

            // Parse Groq's OpenAI-compatible response structure
            JsonNode root = objectMapper.readTree(response.getBody());
            String aiJsonString = root.path("choices").get(0)
                    .path("message").path("content").asText();

            String cleanedJson = aiEvaluationUtil.cleanAiJson(aiJsonString);

            EvaluationResult result = objectMapper.readValue(cleanedJson, EvaluationResult.class);
            evaluationValidationUtil.validateEvaluationResult(result);

            return result;

        } catch (Exception e) {
            EvaluationResult error = new EvaluationResult();
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

    @Override
    public String getProviderName() {
        return "groq";
    }
}
