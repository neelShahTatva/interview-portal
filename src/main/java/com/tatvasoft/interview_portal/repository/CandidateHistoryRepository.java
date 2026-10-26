package com.tatvasoft.interview_portal.repository;

import com.tatvasoft.interview_portal.entity.CandidateHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateHistoryRepository extends JpaRepository<CandidateHistory, Long> {
}
