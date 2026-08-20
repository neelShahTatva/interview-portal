package com.tatvasoft.interview_portal.repository;

import com.tatvasoft.interview_portal.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionsRepository extends JpaRepository<Question, Long> {
    List<Question> findByIdIn(List<Long> ids);
}
