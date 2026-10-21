package com.tatvasoft.interview_portal.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSimilarityResult {
    private boolean isSimilar;
    private Long matchedQuestionId;
    private String reason;
}
