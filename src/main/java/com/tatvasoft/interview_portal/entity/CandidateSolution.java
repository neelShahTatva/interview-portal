package com.tatvasoft.interview_portal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_solutions")
@Getter
@Setter
public class CandidateSolution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "submission_id")
    private Long submissionId;

    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "solution", columnDefinition = "TEXT")
    private String solution;

    @Column(name = "ai_score")
    private Integer aiScore;

    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    private String aiFeedback;

    @Column(name = "time_complexity")
    private String timeComplexity;

    @Column(name = "space_complexity")
    private String spaceComplexity;

    @Column(name = "missed_edge_cases", columnDefinition = "TEXT")
    private String missedEdgeCases;

    @Column(name = "security_issues", columnDefinition = "TEXT")
    private String securityIssues;

    @Column(name = "optimized_code", columnDefinition = "TEXT")
    private String optimizedCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;
}