package com.tatvasoft.interview_portal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assessments")
@Getter
@Setter
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidate_id")
    private Long candidateId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "candidate_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false
    )
    private Candidate candidate;

    @Column(name = "title")
    private String title;

    private String status;

    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

      @OneToMany(
            mappedBy = "assessment",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<AssessmentQuestion> assessmentQuestions =
            new ArrayList<>();
}