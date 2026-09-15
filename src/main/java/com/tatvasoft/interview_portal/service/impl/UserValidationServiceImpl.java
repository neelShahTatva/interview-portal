package com.tatvasoft.interview_portal.service.impl;

import com.tatvasoft.interview_portal.entity.User;
import com.tatvasoft.interview_portal.exception.ResourceNotFoundException;
import com.tatvasoft.interview_portal.exception.UserAlreadyExistsException;
import com.tatvasoft.interview_portal.repository.UserRepository;
import com.tatvasoft.interview_portal.service.UserValidationService;
import org.springframework.stereotype.Service;

@Service
public class UserValidationServiceImpl
        implements UserValidationService {

    private final UserRepository userRepository;

    public UserValidationServiceImpl(
            UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public void validateUsernameAvailable(
            String username) {

        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException(
                    "Username already exists"
            );
        }
    }

    @Override
    public void validateEmailAvailable(
            String email) {

        if (userRepository
                .findByEmail(email)
                .isPresent()) {

            throw new UserAlreadyExistsException(
                    "Email already in use"
            );
        }
    }

    @Override
    public void validateUsernameAvailableForUpdate(
            String username,
            Long currentUserId) {

        userRepository
                .findByUsername(username)
                .ifPresent(existingUser -> {

                    if (!existingUser
                            .getId()
                            .equals(currentUserId)) {

                        throw new UserAlreadyExistsException(
                                "Username already exists"
                        );
                    }
                });
    }

    @Override
    public void validateEmailAvailableForUpdate(
            String email,
            Long currentUserId) {

        userRepository
                .findByEmail(email)
                .ifPresent(existingUser -> {

                    if (!existingUser
                            .getId()
                            .equals(currentUserId)) {

                        throw new UserAlreadyExistsException(
                                "Email already in use"
                        );
                    }
                });
    }

    @Override
    public User getRequiredUser(
            Long userId) {

        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));
    }

    @Override
    public User getRequiredUserByUsername(
            String username) {

        return userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with username: "
                                        + username
                        ));
    }
}