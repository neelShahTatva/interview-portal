package com.tatvasoft.interview_portal.repository;

import com.tatvasoft.interview_portal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameAndIsDeletedFalse(String username);

    boolean existsByUsername(String username);

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    Optional<User> findByResetToken(String resetToken);

    Optional<User> findByIdAndIsDeletedFalse(Long id);
}