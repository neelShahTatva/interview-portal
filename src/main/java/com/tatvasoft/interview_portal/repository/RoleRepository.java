package com.tatvasoft.interview_portal.repository;

import com.tatvasoft.interview_portal.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}