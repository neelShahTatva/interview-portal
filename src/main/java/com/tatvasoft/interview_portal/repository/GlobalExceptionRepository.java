package com.tatvasoft.interview_portal.repository;

import com.tatvasoft.interview_portal.entity.GlobalException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalExceptionRepository extends JpaRepository<GlobalException, Long> {
}
