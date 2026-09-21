package com.tatvasoft.interview_portal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private int statusCode;
    private boolean success;
    private List<String> errorMessages;
    private T result;
}
