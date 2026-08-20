package com.tatvasoft.interview_portal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String difficulty;
    private Integer estimatedTime;
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private Long createdBy = null;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Long updatedBy = null;
    @OneToMany(
            mappedBy = "question",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<QuestionDesignation> designations =
            new ArrayList<>();
    @ManyToMany
    @JoinTable(
            name = "question_categories",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private java.util.Set<Category> categories = new java.util.HashSet<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<QuestionSolution> solutions = new java.util.ArrayList<>();
}