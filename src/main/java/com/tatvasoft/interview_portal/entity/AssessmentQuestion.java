package com.tatvasoft.interview_portal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "assessment_questions")
@Getter
@Setter
public class AssessmentQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "assessment_id")
    private Integer assessmentId;

     @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "assessment_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false
    )
    private Assessment assessment;

    @Column(name = "question_id")
    private Integer questionId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;
}