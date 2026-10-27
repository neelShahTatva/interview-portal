package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        description = "Request payload for deleting a candidate"
)
public class DeleteCandidateRequestDTO {

    @NotBlank(message = "Comment is required")
    @Size(max = 100, message = "Comment must not exceed 100 characters")
    @Schema(
            description = "Reason or comment explaining why the candidate is being deleted",
            example = "Candidate requested account deletion",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 100
    )
    private String comment;
}