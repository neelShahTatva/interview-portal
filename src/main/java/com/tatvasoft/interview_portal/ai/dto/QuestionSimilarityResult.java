package com.tatvasoft.interview_portal.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSimilarityResult {
    private boolean isASimilarQuestion;
    private boolean isUnique;
    @JsonIgnore
    private Long matchedQuestionId;
    private String reason;
    @Builder.Default
    private List<DuplicateQuestionDto> duplicateQuestions = new ArrayList<>();
    @Builder.Default
    private List<DuplicateQuestionDto> redundantQuestions = new ArrayList<>();

    public boolean isSimilar() {
        return isASimilarQuestion;
    }

    public static QuestionSimilarityResult unique(String reason) {
        return QuestionSimilarityResult.builder()
                .isASimilarQuestion(false)
                .isUnique(true)
                .reason(reason)
                .duplicateQuestions(new ArrayList<>())
                .redundantQuestions(new ArrayList<>())
                .build();
    }

    public static QuestionSimilarityResult similar(Long primaryMatchedId, String reason, List<DuplicateQuestionDto> duplicates) {
        List<DuplicateQuestionDto> list = duplicates != null ? duplicates : new ArrayList<>();
        return QuestionSimilarityResult.builder()
                .isASimilarQuestion(true)
                .isUnique(false)
                .matchedQuestionId(primaryMatchedId)
                .reason(reason)
                .duplicateQuestions(list)
                .redundantQuestions(list)
                .build();
    }
}