package com.tatvasoft.interview_portal.service;

import com.tatvasoft.interview_portal.dto.*;
import com.tatvasoft.interview_portal.dto.user.UpdateUserRequest;
import com.tatvasoft.interview_portal.dto.user.UserProfileResponse;
import com.tatvasoft.interview_portal.dto.user.UserRequest;
import com.tatvasoft.interview_portal.dto.user.UserResponse;
import com.tatvasoft.interview_portal.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserRequest request);
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);

    UserProfileResponse getProfile();
    UserProfileResponse updateProfile(ProfileUpdateRequest request);
    String uploadProfilePicture(MultipartFile file);

    // Internal lookups used by other services or security filters
    User getUserByUserName(String username);
    User getUserByEmail(String email);
}