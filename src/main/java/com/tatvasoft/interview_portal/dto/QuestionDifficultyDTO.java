package com.tatvasoft.interview_portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDifficultyDTO {

    private String difficulty;
    private long count;
}