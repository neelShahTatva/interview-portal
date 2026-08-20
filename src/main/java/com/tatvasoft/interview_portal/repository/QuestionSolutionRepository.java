package com.tatvasoft.interview_portal.repository;

import com.tatvasoft.interview_portal.entity.QuestionSolution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionSolutionRepository extends JpaRepository<QuestionSolution, Long> {

    Optional<QuestionSolution>
    findByQuestionIdAndIsActiveTrue(Long questionId);
}