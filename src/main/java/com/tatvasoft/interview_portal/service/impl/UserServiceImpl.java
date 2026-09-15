package com.tatvasoft.interview_portal.service.impl;

import com.tatvasoft.interview_portal.dto.*;
import com.tatvasoft.interview_portal.entity.Role;
import com.tatvasoft.interview_portal.entity.User;
import com.tatvasoft.interview_portal.exception.*;
import com.tatvasoft.interview_portal.mapper.UserMapper;
import com.tatvasoft.interview_portal.repository.RoleRepository;
import com.tatvasoft.interview_portal.repository.UserRepository;
import com.tatvasoft.interview_portal.service.FileStorageService;
import com.tatvasoft.interview_portal.service.UserService;
import com.tatvasoft.interview_portal.service.UserValidationService;
import com.tatvasoft.interview_portal.util.FileValidationUtil;
import com.tatvasoft.interview_portal.util.JwtUtil;
import com.tatvasoft.interview_portal.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final UserValidationService userValidationService;
    private final FileStorageService fileStorageService;
    private final FileValidationUtil fileValidationUtil;


    @Value("${app.upload.dir:uploads/profile-pictures}")
    private String uploadDir;

    public UserServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper,
            JwtUtil jwtUtil,
            UserValidationService userValidationService,
            FileStorageService fileStorageService,
            FileValidationUtil fileValidationUtil) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.userValidationService = userValidationService;
        this.fileStorageService = fileStorageService;
        this.fileValidationUtil = fileValidationUtil;
    }
    private User getCurrentAuthenticatedUser() {

        Long userId =
                SecurityUtil.getCurrentUserId();

        String currentUsername =
                SecurityUtil.getCurrentUsername();

        if (userId != null) {

            return userRepository
                    .findById(userId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "User not found with username: "
                                            + currentUsername
                            ));
        }

        if (currentUsername != null
                && !currentUsername.isBlank()) {

            return userValidationService
                    .getRequiredUserByUsername(
                            currentUsername
                    );
        }

        throw new ResourceNotFoundException(
                "Authenticated user not found"
        );
    }

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {

        userValidationService.validateUsernameAvailable(
                request.getUsername()
        );

        userValidationService.validateEmailAvailable(
                request.getEmail()
        );

        Role role = roleRepository
                .findById(request.getRoleId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role not found"
                        ));

        User currentUser =
                getCurrentAuthenticatedUser();

        User user = userMapper.toEntity(
                request,
                passwordEncoder.encode(
                        request.getPassword()
                ),
                role,
                currentUser.getId()
        );

        User savedUser =
                userRepository.save(user);

        return mapToResponse(savedUser);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToResponse(user);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    // --- Internal Lookups for Security/Other Services ---

    @Override
    public User getUserByUserName(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
    public User login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResourceNotFoundException("Invalid password");
        }

        return user;
    }

    // --- Private Helper Methods ---

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().getId(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getProfilePicture()
        );
    }

    @Override
    public UserResponse updateUser(
            Long id,
            UpdateUserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        user.setUsername(request.getUsername());

        user.setEmail(request.getEmail());

        user.setIsActive(request.getIsActive());

        Role role = roleRepository.findById(
                request.getRoleId()
        ).orElseThrow(() ->
                new ResourceNotFoundException("Role not found"));

        user.setRole(role);

        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy(getCurrentAuthenticatedUser().getId());

        userRepository.save(user);

        return mapToResponse(user);
    }

    @Override
    public UserProfileResponse getProfile() {
        User user = getCurrentAuthenticatedUser();
        return mapToProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(
            ProfileUpdateRequest request) {

        User user =
                getCurrentAuthenticatedUser();

        if (!user.getUsername()
                .equalsIgnoreCase(request.getUsername())) {

            userValidationService
                    .validateUsernameAvailableForUpdate(
                            request.getUsername(),
                            user.getId()
                    );
        }

        if (!user.getEmail()
                .equalsIgnoreCase(request.getEmail())) {

            userValidationService
                    .validateEmailAvailableForUpdate(
                            request.getEmail(),
                            user.getId()
                    );
        }

        user.setUsername(
                request.getUsername()
        );

        user.setEmail(
                request.getEmail()
        );

        user.setUpdatedAt(
                LocalDateTime.now()
        );

        user.setUpdatedBy(
                user.getId()
        );

        if (request.getRoleId() != null) {

            Role role =
                    roleRepository
                            .findById(request.getRoleId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Role not found"
                                    ));

            user.setRole(role);
        }

        if (request.getIsActive() != null) {
            user.setIsActive(
                    request.getIsActive()
            );
        }

        if (request.getPassword() != null
                && !request.getPassword().isBlank()) {

            user.setPassword(
                    passwordEncoder.encode(
                            request.getPassword()
                    )
            );
        }

        User updated =
                userRepository.save(user);

        UserProfileResponse response =
                mapToProfileResponse(updated);

        response.setToken(
                jwtUtil.generateAccessToken(updated)
        );

        return response;
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roleId(user.getRole() != null ? user.getRole().getId() : null)
                .roleName(user.getRole() != null ? user.getRole().getRoleName() : null)
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .profilePictureUrl(user.getProfilePicture())
                .build();
    }

    @Override
    @Transactional
    public String uploadProfilePicture(
            MultipartFile file) {

        String extension =
                fileValidationUtil.validateProfilePicture(
                        file
                );

        User user =
                getCurrentAuthenticatedUser();

        String filename =
                fileStorageService.save(
                        file,
                        uploadDir,
                        extension
                );

        user.setProfilePicture(filename);
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy(user.getId());

        userRepository.save(user);

        return filename;
    }
}