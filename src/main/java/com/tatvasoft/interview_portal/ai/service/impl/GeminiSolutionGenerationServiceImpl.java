package com.tatvasoft.interview_portal.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tatvasoft.interview_portal.ai.service.AiSolutionGenerationService;
import com.tatvasoft.interview_portal.constant.GeminiConstants;
import com.tatvasoft.interview_portal.entity.QuestionSolution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
public class GeminiSolutionGenerationServiceImpl
        implements AiSolutionGenerationService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate =
            new RestTemplate();

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Override
    public QuestionSolution generateSolution(
            String title,
            String description
    ) {
        Long userId =
                (Long) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getDetails();
        try {

            String prompt = GeminiConstants.SOLUTION_GENERATION_USER_PROMPT_TEMPLATE
                    .formatted(title, description);

            ObjectNode root = objectMapper.createObjectNode();

            ObjectNode systemInstructionNode = root.putObject("system_instruction");
            ArrayNode sysParts = systemInstructionNode.putArray("parts");
            sysParts.addObject().put("text", GeminiConstants.SOLUTION_GENERATION_SYSTEM_INSTRUCTION);

            ArrayNode contents = root.putArray("contents");
            ObjectNode content = contents.addObject();
            ArrayNode parts = content.putArray("parts");
            parts.addObject().put("text", prompt);

            String requestBody = objectMapper.writeValueAsString(root);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity =
                    new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            apiUrl + apiKey,
                            entity,
                            String.class
                    );

            JsonNode responseRoot =
                    objectMapper.readTree(response.getBody());

            String generatedCode =
                    responseRoot.path("candidates")
                            .get(0)
                            .path("content")
                            .path("parts")
                            .get(0)
                            .path("text")
                            .asText();

            QuestionSolution solution =
                    new QuestionSolution();

            solution.setSolutionCode(generatedCode);
            solution.setIsActive(true);
            solution.setCreatedAt(LocalDateTime.now());
            solution.setCreatedBy(userId);

            return solution;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String generateFileName(String title) {

        return title
                .replaceAll("[^a-zA-Z0-9]", "")
                + "Solution.java";
    }
}