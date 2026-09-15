package com.tatvasoft.interview_portal.service;

import com.tatvasoft.interview_portal.entity.User;

public interface UserValidationService {

    void validateUsernameAvailable(
            String username
    );

    void validateEmailAvailable(
            String email
    );

    void validateUsernameAvailableForUpdate(
            String username,
            Long currentUserId
    );

    void validateEmailAvailableForUpdate(
            String email,
            Long currentUserId
    );

    User getRequiredUser(Long userId);

    User getRequiredUserByUsername(
            String username
    );
}