package com.tatvasoft.interview_portal.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tatvasoft.interview_portal.ai.dto.BulkQuestionCandidate;
import com.tatvasoft.interview_portal.ai.dto.DuplicateQuestionDto;
import com.tatvasoft.interview_portal.ai.dto.QuestionSimilarityResult;
import com.tatvasoft.interview_portal.ai.service.AiQuestionSimilarityService;
import com.tatvasoft.interview_portal.entity.Question;
import com.tatvasoft.interview_portal.exception.globalException.CommonException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

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
    public QuestionSimilarityResult isSimilarQuestion(String title, String description,
            List<Question> existingQuestions) {
        if (StringUtils.isBlank(title)) {
            return QuestionSimilarityResult.unique("Question title is empty");
        }

        if (existingQuestions == null || existingQuestions.isEmpty()) {
            return QuestionSimilarityResult.unique("No existing questions in database to compare against");
        }

        String trimmedTitle = title.trim();
        String trimmedDesc = description != null ? description.trim() : "";

        // 1. Exact match check (title or description)
        List<DuplicateQuestionDto> exactMatches = new ArrayList<>();
        for (Question existing : existingQuestions) {
            if (existing == null) {
                continue;
            }

            String existingTitle = existing.getTitle() != null ? existing.getTitle().trim() : "";
            String existingDesc = existing.getDescription() != null ? existing.getDescription().trim() : "";

            if (!trimmedTitle.isEmpty() && existingTitle.equalsIgnoreCase(trimmedTitle)) {
                log.info("Exact duplicate question title found with Question ID: {}", existing.getId());
                exactMatches.add(DuplicateQuestionDto.builder()
                        .id(existing.getId())
                        .title(existing.getTitle())
                        .description(existing.getDescription())
                        .reason("Exact match with question title")
                        .build());
            } else if (!trimmedDesc.isEmpty() && !existingDesc.isEmpty()
                    && existingDesc.equalsIgnoreCase(trimmedDesc)) {
                log.info("Exact duplicate question description found with Question ID: {}", existing.getId());
                exactMatches.add(DuplicateQuestionDto.builder()
                        .id(existing.getId())
                        .title(existing.getTitle())
                        .description(existing.getDescription())
                        .reason("Exact match with question description")
                        .build());
            }
        }

        if (!exactMatches.isEmpty()) {
            Long primaryId = exactMatches.get(0).getId();
            String reason = exactMatches.get(0).getReason();
            return QuestionSimilarityResult.similar(primaryId, reason, exactMatches);
        }

        // 2. AI semantic similarity check
        try {
            return checkSemanticSimilarityWithGemini(trimmedTitle, trimmedDesc, existingQuestions);
        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to check question similarity with Gemini AI: {}", e.getMessage(), e);
            throw new CommonException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to communicate with AI question similarity service: " + e.getMessage());
        }
    }

    @Override
    public Map<Integer, QuestionSimilarityResult> checkBulkSimilarity(
            List<BulkQuestionCandidate> candidates,
            List<Question> existingQuestions) {

        Map<Integer, QuestionSimilarityResult> results = new HashMap<>();

        if (candidates == null || candidates.isEmpty()) {
            return results;
        }

        List<Question> safeExistingQuestions = existingQuestions != null ? existingQuestions : Collections.emptyList();
        List<BulkQuestionCandidate> remainingForAi = new ArrayList<>();

        // 1. Exact match checks: intra-batch and against database
        for (int i = 0; i < candidates.size(); i++) {
            BulkQuestionCandidate current = candidates.get(i);
            if (current == null || StringUtils.isBlank(current.getTitle())) {
                results.put(current != null ? current.getQuestionNumber() : i + 1,
                        QuestionSimilarityResult.unique("Title is empty"));
                continue;
            }

            String currentTitle = current.getTitle().trim();
            String currentDesc = current.getDescription() != null ? current.getDescription().trim() : "";

            // Check intra-file duplicate with earlier candidates
            boolean matchedIntraFile = false;
            for (int j = 0; j < i; j++) {
                BulkQuestionCandidate prev = candidates.get(j);
                if (prev == null || StringUtils.isBlank(prev.getTitle())) {
                    continue;
                }
                String prevTitle = prev.getTitle().trim();
                String prevDesc = prev.getDescription() != null ? prev.getDescription().trim() : "";

                if (currentTitle.equalsIgnoreCase(prevTitle)
                        || (!currentDesc.isEmpty() && !prevDesc.isEmpty() && currentDesc.equalsIgnoreCase(prevDesc))) {
                    String reason = String.format("Duplicate of Question %d (Row %d) in uploaded file: '%s'",
                            prev.getQuestionNumber(), prev.getRowNumber(), prev.getTitle());
                    DuplicateQuestionDto dup = DuplicateQuestionDto.builder()
                            .title(prev.getTitle())
                            .description(prev.getDescription())
                            .reason(reason)
                            .build();
                    results.put(current.getQuestionNumber(), QuestionSimilarityResult.similar(null, reason, List.of(dup)));
                    matchedIntraFile = true;
                    break;
                }
            }

            if (matchedIntraFile) {
                continue;
            }

            // Check exact match with database questions
            boolean matchedDb = false;
            for (Question existing : safeExistingQuestions) {
                if (existing == null) {
                    continue;
                }
                String existingTitle = existing.getTitle() != null ? existing.getTitle().trim() : "";
                String existingDesc = existing.getDescription() != null ? existing.getDescription().trim() : "";

                if (currentTitle.equalsIgnoreCase(existingTitle)
                        || (!currentDesc.isEmpty() && !existingDesc.isEmpty() && currentDesc.equalsIgnoreCase(existingDesc))) {
                    String reason = String.format("Exact match with existing Question ('%s')", existing.getTitle());
                    DuplicateQuestionDto dup = DuplicateQuestionDto.builder()
                            .id(existing.getId())
                            .title(existing.getTitle())
                            .description(existing.getDescription())
                            .reason(reason)
                            .build();
                    results.put(current.getQuestionNumber(),
                            QuestionSimilarityResult.similar(existing.getId(), reason, List.of(dup)));
                    matchedDb = true;
                    break;
                }
            }

            if (!matchedDb) {
                remainingForAi.add(current);
            }
        }

        if (remainingForAi.isEmpty()) {
            return results;
        }

        if (safeExistingQuestions.isEmpty()) {
            for (BulkQuestionCandidate c : remainingForAi) {
                results.put(c.getQuestionNumber(),
                        QuestionSimilarityResult.unique("No existing questions in database to compare against"));
            }
            return results;
        }

        // 2. AI semantic similarity check for remaining candidates
        try {
            Map<Integer, QuestionSimilarityResult> aiResults =
                    checkBulkSemanticSimilarityWithGemini(remainingForAi, safeExistingQuestions);
            results.putAll(aiResults);
        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to check bulk question similarity with Gemini AI: {}", e.getMessage(), e);
            throw new CommonException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to communicate with AI question similarity service: " + e.getMessage());
        }

        // Ensure any candidate not explicitly returned by AI is populated
        for (BulkQuestionCandidate c : remainingForAi) {
            results.putIfAbsent(c.getQuestionNumber(), QuestionSimilarityResult.unique("Unique question"));
        }

        return results;
    }

    private Map<Integer, QuestionSimilarityResult> checkBulkSemanticSimilarityWithGemini(
            List<BulkQuestionCandidate> candidates,
            List<Question> existingQuestions) throws Exception {

        Map<Integer, QuestionSimilarityResult> results = new HashMap<>();

        StringBuilder candidateList = new StringBuilder();
        for (BulkQuestionCandidate c : candidates) {
            candidateList.append(String.format("""
                    - Question Number: %d (Row: %d)
                      Title: %s
                      Description: %s
                    """,
                    c.getQuestionNumber(),
                    c.getRowNumber(),
                    c.getTitle().replace("\"", "'"),
                    c.getDescription() != null ? c.getDescription().replace("\"", "'") : ""));
        }

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
                You are an AI assistant helping an interview portal detect duplicate or semantically similar technical interview questions during bulk Excel upload.

                A question is considered similar/duplicate if it tests the exact same algorithmic problem, coding logic, or conceptual scenario (even if written with different problem stories, variable names, rephrased descriptions, or slight wording modifications).

                Candidate Questions to check from uploaded Excel:
                %s

                Existing Active Questions in Database:
                %s

                Instructions:
                1. Compare each Candidate Question against the Existing Questions in the Database and also against other Candidate Questions.
                2. For each Candidate Question, determine if it is similar/duplicate to any existing database question OR any earlier candidate question.
                3. Return ONLY valid JSON in the format:
                {
                  "results": [
                    {
                      "questionNumber": 1,
                      "isSimilar": true,
                      "matchedQuestionId": 123,
                      "matchedQuestionTitle": "Two Sum",
                      "reason": "Brief reason explaining why it is similar"
                    },
                    {
                      "questionNumber": 2,
                      "isSimilar": false,
                      "matchedQuestionId": null,
                      "matchedQuestionTitle": null,
                      "reason": "Unique question"
                    }
                  ]
                }
                Do not include markdown or explanations outside the JSON.
                """,
                candidateList,
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
            log.error("Gemini API returned non-2xx status during bulk check: {}", response.getStatusCode());
            throw new CommonException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Gemini AI service returned status: " + response.getStatusCode()
            );
        }

        JsonNode responseRoot = objectMapper.readTree(response.getBody());
        JsonNode candidateNodes = responseRoot.path("candidates");
        if (candidateNodes.isEmpty()) {
            log.error("Gemini API response contained no candidates during bulk check");
            throw new CommonException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Gemini AI service returned an empty response"
            );
        }

        String responseText = candidateNodes.get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();

        String cleanedJson = cleanJson(responseText);
        JsonNode resultRootNode = objectMapper.readTree(cleanedJson);
        JsonNode resultsArray = resultRootNode.path("results");

        Map<Long, Question> questionMap = existingQuestions.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Question::getId, q -> q, (q1, q2) -> q1));

        if (resultsArray.isArray()) {
            for (JsonNode item : resultsArray) {
                int qNum = item.path("questionNumber").asInt(-1);
                if (qNum <= 0) {
                    continue;
                }
                boolean isSimilar = item.path("isSimilar").asBoolean(false);
                String reason = item.path("reason").asText("Semantically similar question detected by AI");

                if (isSimilar) {
                    Long matchedId = item.has("matchedQuestionId") && !item.path("matchedQuestionId").isNull()
                            ? item.path("matchedQuestionId").asLong() : null;
                    String matchedTitle = item.path("matchedQuestionTitle").asText("");

                    Question matchedQuestion = matchedId != null ? questionMap.get(matchedId) : null;
                    if (matchedQuestion != null && matchedTitle.isEmpty()) {
                        matchedTitle = matchedQuestion.getTitle();
                    }

                    DuplicateQuestionDto dup = DuplicateQuestionDto.builder()
                            .id(matchedId)
                            .title(matchedTitle)
                            .description(matchedQuestion != null ? matchedQuestion.getDescription() : "")
                            .reason(reason)
                            .build();

                    results.put(qNum, QuestionSimilarityResult.similar(matchedId, reason, List.of(dup)));
                } else {
                    results.put(qNum, QuestionSimilarityResult.unique(reason));
                }
            }
        }

        return results;
    }

    private QuestionSimilarityResult checkSemanticSimilarityWithGemini(
            String title,
            String description,
            List<Question> existingQuestions) throws Exception {

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
                2. If the Candidate Question is semantically identical or essentially the same problem as any existing question, set "isSimilar": true, list all matched existing question IDs in "matchedQuestionIds", and provide a clear "reason".
                3. If the Candidate Question is unique, set "isSimilar": false and "matchedQuestionIds": [].
                4. Return ONLY valid JSON in the format:
                {
                  "isSimilar": true,
                  "matchedQuestionIds": [123],
                  "reason": "Brief reason explaining why it is similar"
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
            log.error("Gemini API returned non-2xx status: {}", response.getStatusCode());
            throw new CommonException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Gemini AI service returned status: " + response.getStatusCode()
            );
        }

        JsonNode responseRoot = objectMapper.readTree(response.getBody());
        JsonNode candidates = responseRoot.path("candidates");
        if (candidates.isEmpty()) {
            log.error("Gemini API response contained no candidates");
            throw new CommonException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Gemini AI service returned an empty response"
            );
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

        if (!isSimilar) {
            return QuestionSimilarityResult.unique("No duplicate or similar questions found");
        }

        List<Long> matchedIds = new ArrayList<>();
        if (resultNode.has("matchedQuestionIds") && resultNode.path("matchedQuestionIds").isArray()) {
            for (JsonNode idNode : resultNode.path("matchedQuestionIds")) {
                matchedIds.add(idNode.asLong());
            }
        }
        if (matchedIds.isEmpty() && resultNode.has("matchedQuestionId")) {
            long singleId = resultNode.path("matchedQuestionId").asLong();
            if (singleId > 0) {
                matchedIds.add(singleId);
            }
        }

        String reason = resultNode.path("reason").asText("Semantically similar question detected by AI");

        Map<Long, Question> questionMap = existingQuestions.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Question::getId, q -> q, (q1, q2) -> q1));

        List<DuplicateQuestionDto> duplicates = new ArrayList<>();
        for (Long id : matchedIds) {
            Question matchedQuestion = questionMap.get(id);
            if (matchedQuestion != null) {
                duplicates.add(DuplicateQuestionDto.builder()
                        .id(matchedQuestion.getId())
                        .title(matchedQuestion.getTitle())
                        .description(matchedQuestion.getDescription())
                        .reason(reason)
                        .build());
            } else {
                duplicates.add(DuplicateQuestionDto.builder()
                        .id(id)
                        .title("Question ID " + id)
                        .description("")
                        .reason(reason)
                        .build());
            }
        }

        Long primaryId = !matchedIds.isEmpty() ? matchedIds.get(0) : null;
        log.info("AI detected similar question. Primary Matched ID: {}, Duplicates count: {}, Reason: {}",
                primaryId, duplicates.size(), reason);

        return QuestionSimilarityResult.similar(primaryId, reason, duplicates);
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