package com.tatvasoft.interview_portal.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tatvasoft.interview_portal.ai.dto.EvaluationResult;
import com.tatvasoft.interview_portal.ai.dto.FileSubmissionRequest;
import com.tatvasoft.interview_portal.ai.service.AiProviderService;
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
    @Override
    public EvaluationResult evaluateCode(FileSubmissionRequest request) {
        try {
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

            String systemInstruction = """
                You are a strict Senior Java Technical Interviewer.
                Return ONLY a raw JSON object matching this exact structure. Do not include markdown tags like ```json.
                {
                  "score": <integer between 0 and 10>,
                  "feedback": "<string: detailed overall critique>",
                  "timeComplexity": "<string: Big-O notation>",
                  "spaceComplexity": "<string: Big-O notation>",
                  "missedEdgeCases": ["<string>", "<string>"],
                  "securityIssues": ["<string>", "<string>"],
                  "optimizedCode": "<string: the perfect production-ready Java code>"
                }
                """.trim();

            String userPrompt = String.format(
                    """
                    Question Topic: %s
    
                    Reference / Existing Solution:
                    %s
    
                    Candidate Submission:
                    %s
    
                    Compare the candidate's code against the reference solution.
                    Evaluate for correctness, performance, thread-safety, and edge cases.
                    Be brutal but fair. Provide the optimized, production-ready version if their code is flawed.
                    """,
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

            return objectMapper.readValue(aiJsonString, EvaluationResult.class);

        } catch (Exception e) {
            EvaluationResult error = new EvaluationResult();
            error.setFeedback("Groq API Error: " + e.getMessage());
            return error;
        }
    }

    @Override
    public String getProviderName() {
        return "groq";
    }
}
