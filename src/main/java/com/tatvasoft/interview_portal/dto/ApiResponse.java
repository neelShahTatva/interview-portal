package com.tatvasoft.interview_portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private int statusCode;
    private boolean success;
    private List<String> errorMessages;
    private T result;
}
