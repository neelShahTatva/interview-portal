package com.tatvasoft.interview_portal.entity;

import com.tatvasoft.interview_portal.enums.ActionPerformed;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "candidate_id")
    private long candidateId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String email;

    private Integer experience;

    private String designation;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    private String comment;

    @Column(name = "last_appeared_at")
    private LocalDateTime lastAppearedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private String result;

    @Column(name = "ai_score")
    private Integer aiScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_performed")
    private ActionPerformed actionPerformed;
}
