package com.tatvasoft.interview_portal.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@Service("geminiService")
public class GeminiEvaluationServiceImpl implements AiProviderService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

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

            String promptText = String.format("""
                You are a strict Senior Java Technical Interviewer.
                Return ONLY a raw JSON object matching this exact structure. Do not include markdown tags like ```json.
                {
                  "score": <integer between 0 and 10>,
                  "feedback": "<string: detailed overall critique>",
                  "timeComplexity": "<ONLY Big-O notation like O(1), O(log N), O(N), O(N log N), O(N²)>",
                  "spaceComplexity": "<ONLY Big-O notation like O(1), O(log N), O(N), O(N log N), O(N²)>",
                  "missedEdgeCases": ["<string>", "<string>"],
                  "securityIssues": ["<string>", "<string>"],
                  "optimizedCode": "<string: the perfect production-ready Java code>"
                }
                IMPORTANT RULES:
                - score must be an integer from 0 to 10.
                - timeComplexity must contain ONLY the Big-O notation. No explanations.
                - spaceComplexity must contain ONLY the Big-O notation. No explanations.
    
                Question Topic: %s

                Reference / Existing Solution:
                %s

                Candidate Submission:
                %s

                Compare the candidate's code against the reference solution.
                Evaluate for correctness, performance, thread-safety, and edge cases.
                Be brutal but fair. Provide the optimized version if theirs is flawed.
                """,
                    question.getDescription(),
                    solutionCode,
                    candidateCode
            );

            String requestBody = """
                {
                  "contents": [{
                    "parts": [{"text": "%s"}]
                  }]
                }
                """.formatted(
                    promptText
                            .replace("\\", "\\\\")
                            .replace("\"", "\\\"")
                            .replace("\n", "\\n")
                            .replace("\r", "")
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            int maxRetries = 3;
            long waitTime = 2000;

            for (int attempt = 0; attempt < maxRetries; attempt++) {
                try {
                    ResponseEntity<String> response = restTemplate.postForEntity(
                            apiUrl + apiKey, entity, String.class
                    );

                    JsonNode root = objectMapper.readTree(response.getBody());
                    String aiJson = root.path("candidates").get(0)
                            .path("content").path("parts").get(0)
                            .path("text").asText();

                    return objectMapper.readValue(aiJson, EvaluationResult.class);

                } catch (Exception e) {
                    String msg = e.getMessage() != null ? e.getMessage() : "";
                    if (msg.contains("503") || msg.contains("429")) {
                        System.out.println("Gemini busy. Retry " + (attempt + 1) + " in " + waitTime + "ms");
                        Thread.sleep(waitTime);
                        waitTime *= 2;
                    } else {
                        throw e; // non-retryable error
                    }
                }
            }

            EvaluationResult busy = new EvaluationResult();
            busy.setFeedback("Gemini is currently too busy. Please try again in a few minutes.");
            return busy;

        } catch (Exception e) {
            EvaluationResult error = new EvaluationResult();
            error.setFeedback("File evaluation failed: " + e.getMessage());
            return error;
        }
    }

    @Override
    public String getProviderName() {
        return "gemini";
    }

}