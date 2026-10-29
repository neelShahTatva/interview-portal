package com.tatvasoft.interview_portal.repository;

import com.tatvasoft.interview_portal.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionsRepository extends JpaRepository<Question, Long> {
    List<Question> findByIdIn(List<Long> ids);

    Optional<Question> findByIdAndIsDeletedFalse(Long id);
}
