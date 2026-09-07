package com.tatvasoft.interview_portal.repository;

import com.tatvasoft.interview_portal.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    Optional<Assessment> findByCandidateId(Long candidateId);

    java.util.List<Assessment> findAllByCandidateId(Long candidateId);

    boolean existsByCandidateIdAndIsActiveTrue(
            Long candidateId
    );

    List<Assessment> findByIsActiveTrue();

    Optional<Assessment> findByCandidateIdAndIsActiveTrue(Long candidateId);

}
