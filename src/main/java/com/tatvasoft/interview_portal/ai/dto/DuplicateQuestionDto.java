package com.tatvasoft.interview_portal.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateQuestionDto {
    @JsonIgnore
    private Long id;
    private String title;
    private String description;
    private String reason;
}
