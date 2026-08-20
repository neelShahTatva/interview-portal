package com.tatvasoft.interview_portal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
@Getter
@Setter
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assessment_id")
    private Long assessmentId;

    @Column(name = "reference_file_id")
    private Long referenceFileId;

    @Column(name = "candidate_id")
    private Long candidateId;

    private String code;

    private String output;

    @Column(name = "ai_score")
    private Integer aiScore;

    @Column(name = "ai_feedback")
    private String aiFeedback;

    @Column(name = "status")
    private String status;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;
}
