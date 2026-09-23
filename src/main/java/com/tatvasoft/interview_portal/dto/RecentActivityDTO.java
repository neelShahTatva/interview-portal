package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Recent audit/activity log entry")
public class RecentActivityDTO {

    @Schema(description = "Activity type (e.g. ASSESSMENT_CREATED, CANDIDATE_EVALUATED)", example = "ASSESSMENT_CREATED")
    private String type;

    @Schema(description = "Description of the activity performed", example = "Assessment created for John Doe")
    private String description;

    @Schema(description = "Name of the associated entity", example = "Java Developer Assessment")
    private String entityName;

    @Schema(description = "Activity timestamp")
    private LocalDateTime timestamp;
}