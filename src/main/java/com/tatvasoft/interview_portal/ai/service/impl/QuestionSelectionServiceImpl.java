package com.tatvasoft.interview_portal.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tatvasoft.interview_portal.ai.service.QuestionSelectionService;
import com.tatvasoft.interview_portal.entity.Question;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionSelectionServiceImpl
        implements QuestionSelectionService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate =
            new RestTemplate();

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Override
    public List<Long> selectQuestions(
            String designation,
            List<Question> questions,
            int maxMinutes) {

        try {

            String questionData =
                    questions.stream()
                            .map(q ->
                                    String.format(
                                            """
                                            {
                                              "id": %d,
                                              "title": "%s",
                                              "difficulty": "%s",
                                              "estimatedMinutes": %d
                                            }
                                            """,
                                            q.getId(),
                                            q.getTitle(),
                                            q.getDifficulty(),
                                            q.getEstimatedTime()
                                    ))
                            .collect(Collectors.joining(",\n"));

            String prompt = String.format("""
                    You are a Senior Technical Interviewer.

                    Select interview questions for a %s candidate.

                    Rules:
                    1. Total estimated duration MUST NOT exceed %d minutes.
                    2. Maintain this difficulty distribution as much as possible:
                       - Easy: 30%%
                       - Medium: 40%%
                       - Hard: 30%%
                    3. Return ONLY valid JSON.
                    4. Do NOT include explanations.
                    5. Select questions most relevant to the candidate designation.

                    Return JSON in exactly this format:

                    {
                      "questionIds": [1, 2, 5]
                    }

                    Available Questions:

                    [
                    %s
                    ]
                    """,
                    designation,
                    maxMinutes,
                    questionData
            );

            String requestBody = """
                    {
                      "contents": [{
                        "parts": [{
                          "text": "%s"
                        }]
                      }]
                    }
                    """
                    .formatted(
                            prompt
                                    .replace("\\", "\\\\")
                                    .replace("\"", "\\\"")
                                    .replace("\n", "\\n")
                                    .replace("\r", "")
                    );

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_JSON
            );

            HttpEntity<String> entity =
                    new HttpEntity<>(
                            requestBody,
                            headers
                    );

            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            apiUrl + apiKey,
                            entity,
                            String.class
                    );

            JsonNode geminiResponse =
                    objectMapper.readTree(
                            response.getBody()
                    );

            String aiResponse =
                    geminiResponse
                            .path("candidates")
                            .get(0)
                            .path("content")
                            .path("parts")
                            .get(0)
                            .path("text")
                            .asText();

            aiResponse = aiResponse
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            JsonNode result =
                    objectMapper.readTree(
                            aiResponse
                    );

            List<Long> questionIds =
                    new ArrayList<>();

            JsonNode ids =
                    result.get("questionIds");

            if (ids == null || !ids.isArray()) {
                throw new RuntimeException(
                        "Invalid Gemini response format"
                );
            }

            ids.forEach(node ->
                    questionIds.add(
                            node.asLong()
                    )
            );

            return questionIds;

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Failed to recommend questions using Gemini: "
                            + ex.getMessage(),
                    ex
            );
        }
    }
}