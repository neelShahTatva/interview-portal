package com.tatvasoft.interview_portal.repository;

import com.tatvasoft.interview_portal.entity.Assessment;
import com.tatvasoft.interview_portal.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateRepository
        extends JpaRepository<Candidate, Long> {

    boolean existsByEmail(String email);

}