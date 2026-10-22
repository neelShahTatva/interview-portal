package com.tatvasoft.interview_portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteCandidateRequestDTO {

    @NotBlank(message = "Comment is required")
    @Size(max = 100, message = "Comment must not exceed 100 characters")
    private String comment;
}