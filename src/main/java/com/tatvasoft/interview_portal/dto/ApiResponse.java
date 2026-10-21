package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Schema(description = "Standard API response wrapper")
public class ApiResponse<T> {

    @Schema(description = "HTTP status code", example = "200")
    private int statusCode;

    @Schema(description = "Indicates whether the request was successful", example = "true")
    private boolean success;

    @Schema(description = "List of error messages if any", example = "null")
    private List<String> errorMessages;

    @Schema(description = "Response payload / data")
    private T result;
}
