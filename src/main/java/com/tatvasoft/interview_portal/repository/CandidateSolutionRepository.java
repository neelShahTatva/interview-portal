package com.tatvasoft.interview_portal.repository;

import com.tatvasoft.interview_portal.entity.CandidateSolution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateSolutionRepository
        extends JpaRepository<CandidateSolution, Long> {

    List<CandidateSolution>
    findBySubmissionId(Long submissionId);
}
