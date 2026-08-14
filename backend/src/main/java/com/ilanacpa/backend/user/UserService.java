package com.ilanacpa.backend.user;

import com.ilanacpa.backend.auth.RefreshTokenService;
import com.ilanacpa.backend.common.BadRequestException;
import com.ilanacpa.backend.common.NotFoundException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    public UserService(UserRepository userRepository, RefreshTokenService refreshTokenService,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(normalize(email));
    }

    public User getById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User createUser(String email, String fullName, Role role, String rawPassword) {
        String normalized = normalize(email);
        if (userRepository.existsByEmail(normalized)) {
            throw new BadRequestException("A user with this email already exists");
        }
        User user = new User();
        user.setEmail(normalized);
        user.setFullName(fullName);
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    public record CreatedUser(User user, String temporaryPassword) {
    }

    public CreatedUser createUserWithTemporaryPassword(String email, String fullName, Role role) {
        String temporaryPassword = generateTemporaryPassword();
        User user = createUser(email, fullName, role, temporaryPassword);
        return new CreatedUser(user, temporaryPassword);
    }

    private String generateTemporaryPassword() {
        byte[] bytes = new byte[9];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public User updateRoleAndStatus(UUID userId, Role role, UserStatus status) {
        User user = getById(userId);
        boolean changed = user.getRole() != role || user.getStatus() != status;
        user.setRole(role);
        user.setStatus(status);
        User saved = userRepository.save(user);
        if (changed) {
            // Role/status changes must not be usable via an already-issued refresh token.
            refreshTokenService.revokeAllForUser(userId);
        }
        return saved;
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }
}
