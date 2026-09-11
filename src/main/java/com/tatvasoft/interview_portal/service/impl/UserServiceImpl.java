package com.tatvasoft.interview_portal.service.impl;

import com.tatvasoft.interview_portal.dto.*;
import com.tatvasoft.interview_portal.entity.Role;
import com.tatvasoft.interview_portal.entity.User;
import com.tatvasoft.interview_portal.exception.*;
import com.tatvasoft.interview_portal.mapper.UserMapper;
import com.tatvasoft.interview_portal.repository.RoleRepository;
import com.tatvasoft.interview_portal.repository.UserRepository;
import com.tatvasoft.interview_portal.service.UserService;
import com.tatvasoft.interview_portal.util.JwtUtil;
import com.tatvasoft.interview_portal.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @Value("${app.upload.dir:uploads/profile-pictures}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp"
    );

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           UserMapper userMapper,
                           JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    private User getCurrentAuthenticatedUser() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId != null) {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        }
        String currentUsername = SecurityUtil.getCurrentUsername();
        if (currentUsername != null) {
            return getUserByUserName(currentUsername);
        }
        throw new ResourceNotFoundException("Authenticated user not found");
    }

    @Override
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

         if (userRepository.findByEmail(request.getEmail()).isPresent()) {
             throw new UserAlreadyExistsException("Email already in use");
         }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        User currentUser = getCurrentAuthenticatedUser();

        User user = userMapper.toEntity(
                request,
                passwordEncoder.encode(request.getPassword()),
                role,
                currentUser.getId()
        );

        User savedUser = userRepository.save(user);
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
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
            UserRequest request) {

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

        // optional password update

        if (request.getPassword() != null
                && !request.getPassword().isBlank()) {

            user.setPassword(
                    passwordEncoder.encode(
                            request.getPassword()
                    )
            );
        }

        userRepository.save(user);

        return mapToResponse(user);
    }

    @Override
    public UserProfileResponse getProfile() {
        User user = getCurrentAuthenticatedUser();
        return mapToProfileResponse(user);
    }

    @Override
    public UserProfileResponse updateProfile(ProfileUpdateRequest request) {
        User user = getCurrentAuthenticatedUser();

        if (!user.getUsername().equalsIgnoreCase(request.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already in use");
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy(user.getId());

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
            user.setRole(role);
        }

        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updated = userRepository.save(user);
        UserProfileResponse response = mapToProfileResponse(updated);
        response.setToken(jwtUtil.generateAccessToken(updated));
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
    public String uploadProfilePicture(MultipartFile file) {
        //  file must not be null/empty
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file provided. Please select an image to upload.");
        }

        // file size ≤ 5 MB 
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File size exceeds the 5 MB limit. Please upload a smaller image.");
        }

        // MIME / Content-Type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid file type. Only JPEG, PNG, and WEBP images are allowed.");
        }

        // file extension
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new IllegalArgumentException("File must have a valid extension (jpg, jpeg, png, webp).");
        }
        String extension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Invalid file extension. Allowed: jpg, jpeg, png, webp.");
        }

        // magic bytes
        try {
            byte[] header = new byte[4];
            int read = file.getInputStream().read(header);
            if (read < 4 || !isValidImageSignature(header, extension)) {
                throw new IllegalArgumentException(
                        "File content does not match its declared type. Please upload a real image.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read file content. Please try again.");
        }

        // Resolve current user 
        User user = getCurrentAuthenticatedUser();

        // Save file to disk
        try {
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);


            // Filename format: {yyyyMMdd_HHmmss}_{uuid6}.{ext}
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = dateStr + "_" + UUID.randomUUID().toString().substring(0, 6) + "." + extension;
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Persist only filename to DB 
            user.setProfilePicture(filename);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            return filename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to save profile picture. Please try again.", e);
        }
    }

    private boolean isValidImageSignature(byte[] header, String extension) {
        return switch (extension) {
            case "jpg", "jpeg" ->
                    (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF;
            case "png" ->
                    (header[0] & 0xFF) == 0x89 && (header[1] & 0xFF) == 0x50
                            && (header[2] & 0xFF) == 0x4E && (header[3] & 0xFF) == 0x47;
            case "webp" ->
                    // RIFF header
                    (header[0] & 0xFF) == 0x52 && (header[1] & 0xFF) == 0x49
                            && (header[2] & 0xFF) == 0x46 && (header[3] & 0xFF) == 0x46;
            default -> false;
        };
    }
}