package com.tatvasoft.interview_portal.ai.dto;

import lombok.Data;

import java.util.List;

@Data
public class EvaluationResult {
    private int score;
    private String feedback;
    private String timeComplexity;         // e.g., "O(n^2)", "O(log n)"
    private String spaceComplexity;        // e.g., "O(1)", "O(n)"

    private List<String> missedEdgeCases;  // e.g., ["Null list passed", "Negative numbers"]
    private List<String> securityIssues;   // e.g., ["Thread-unsafe map", "SQL Injection risk"]

    private String optimizedCode;
}
