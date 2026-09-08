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

            String requestBody = buildGeminiRequest(promptText);

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

        return """
                You are a strict but fair Senior Java Technical Interviewer.

                Your task is to evaluate ONLY the candidate's Java coding
                submission.

                ============================================================
                QUESTION
                ============================================================

                %s

                ============================================================
                REFERENCE SOLUTION
                ============================================================

                %s

                ============================================================
                CANDIDATE SUBMISSION
                ============================================================

                %s

                ============================================================
                IMPORTANT EVALUATION PRINCIPLES
                ============================================================

                1. Evaluate the candidate against the actual requirements
                   of the QUESTION.

                2. The reference solution is only a reference for the
                   expected behavior.

                3. DO NOT require the candidate to implement the exact same
                   algorithm or code structure as the reference solution.

                4. A different implementation must receive full credit if
                   it correctly solves the problem.

                5. Do not invent requirements that are not stated or
                   reasonably implied by the question.

                6. Inspect the actual candidate code before identifying
                   problems.

                7. Do not provide vague feedback.

                8. Every criticism must refer to a concrete issue in the
                   candidate's submission.

                9. Do not invent security issues.

                10. Do not invent edge cases.

                11. If there is no concrete security problem, return [].

                12. If no important edge case is missed, return [].

                13. Do not give a high score simply because the candidate
                    has a generally correct idea.

                14. The score MUST match the severity of the actual issues.

                ============================================================
                SCORE GUIDELINES
                ============================================================

                10:
                Exceptional solution.

                Fully correct, efficient, robust, clean and production
                quality.

                There must be no meaningful correctness, edge-case,
                performance or code-quality problem.

                9:
                Excellent solution.

                Correct and efficient with only a very minor issue that
                does not meaningfully affect correctness.

                8:
                Good solution.

                Correct overall, but has a noticeable weakness such as
                a minor edge case, unnecessary complexity, or code-quality
                concern.

                7:
                Mostly good solution.

                Core functionality is correct but there are meaningful
                weaknesses that should be addressed.

                6:
                Partially strong solution.

                Main approach is correct, but there are important issues
                affecting robustness, efficiency or completeness.

                5:
                Borderline solution.

                Some significant functionality works, but important
                problems exist.

                3-4:
                Weak solution.

                Significant correctness or design problems exist.

                1-2:
                Very weak solution.

                Minimal useful implementation or major misunderstanding.

                0:
                Completely incorrect, empty, unrelated, or non-functional.

                ============================================================
                CRITICAL SCORING RULE
                ============================================================

                NEVER give 9 or 10 if the candidate has a meaningful
                correctness issue.

                NEVER give 10 if missedEdgeCases is non-empty and the
                missed edge case affects the correctness of the solution.

                NEVER give 10 if the feedback describes a significant
                performance problem.

                NEVER give 10 if the feedback describes a significant
                code-quality or implementation problem.

                If the candidate has a critical correctness issue,
                score MUST be 5 or lower.

                The score and feedback MUST NOT contradict each other.

                ============================================================
                FEEDBACK REQUIREMENTS
                ============================================================

                Feedback must contain:

                1. A short assessment of whether the solution is correct.

                2. What the candidate did well.

                3. Specific problems found in the code.

                4. Why those problems matter.

                5. What should be improved.

                Do NOT write generic statements such as:

                "The code can be improved."

                Instead write concrete feedback such as:

                "The solution performs a nested loop over the input, resulting
                in O(N²) time. This can be reduced to O(N) by using a HashSet."

                Do not mention issues that do not exist.

                ============================================================
                EDGE CASE RULES
                ============================================================

                Only include edge cases that:

                - Are relevant to the question.
                - Are required or reasonably expected.
                - Are actually missed by the candidate.

                Otherwise return an empty array.

                ============================================================
                SECURITY RULES
                ============================================================

                Only report a security issue if a concrete security
                vulnerability exists in the candidate code.

                Do not report theoretical or irrelevant security concerns.

                If no real security issue exists:

                "securityIssues": []

                ============================================================
                COMPLEXITY RULES
                ============================================================

                timeComplexity must contain ONLY the Big-O expression.

                Examples:

                O(1)
                O(log N)
                O(N)
                O(N log N)
                O(N²)
                O(2^N)
                O(N!)
                O(V + E)
                O(N + M)

                Do not write explanations inside timeComplexity.

                spaceComplexity must contain ONLY the Big-O expression.

                ============================================================
                OPTIMIZED CODE RULES
                ============================================================

                If the candidate solution is already correct, efficient
                and reasonably clean, return:

                "optimizedCode": null

                Do NOT generate replacement code simply because you can
                write it differently.

                If the candidate has a meaningful implementation problem,
                return a corrected Java implementation.

                ============================================================
                JSON FORMAT
                ============================================================

                Return ONLY a valid JSON object.

                Do NOT use markdown.

                Do NOT use ```json.

                Do NOT include any text before or after the JSON.

                The JSON MUST have exactly these fields:

                {
                  "score": 0,
                  "feedback": "Detailed specific evaluation",
                  "timeComplexity": "O(N)",
                  "spaceComplexity": "O(1)",
                  "missedEdgeCases": [],
                  "securityIssues": [],
                  "optimizedCode": null
                }

                ============================================================
                FINAL SELF-CHECK BEFORE RETURNING JSON
                ============================================================

                Before returning the result, verify:

                - Is the candidate actually solving the question?
                - Is the score consistent with the feedback?
                - If score is 10, are there genuinely no meaningful issues?
                - Are missedEdgeCases based on actual missing handling?
                - Are securityIssues based on actual vulnerabilities?
                - Is timeComplexity ONLY Big-O notation?
                - Is spaceComplexity ONLY Big-O notation?
                - Is optimizedCode null when no meaningful optimization
                  is necessary?
                - Is the response valid JSON?

                Return ONLY the JSON.

                """.formatted(
                question,
                referenceCode,
                candidateCode
        );
    }

    private String buildGeminiRequest(
            String promptText) throws Exception {

        ObjectNode root =
                objectMapper.createObjectNode();

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