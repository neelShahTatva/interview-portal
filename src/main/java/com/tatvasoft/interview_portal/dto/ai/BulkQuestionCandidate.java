package com.tatvasoft.interview_portal.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkQuestionCandidate {
    private int questionNumber;
    private int rowNumber;
    private String title;
    private String description;
}
