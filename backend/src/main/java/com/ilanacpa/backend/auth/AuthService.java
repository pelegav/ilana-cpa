package com.ilanacpa.backend.auth;

import com.ilanacpa.backend.audit.AuditAction;
import com.ilanacpa.backend.audit.AuditService;
import com.ilanacpa.backend.auth.dto.TokenPairResponse;
import com.ilanacpa.backend.auth.dto.UserSummary;
import com.ilanacpa.backend.common.NotFoundException;
import com.ilanacpa.backend.security.JwtService;
import com.ilanacpa.backend.user.User;
import com.ilanacpa.backend.user.UserRepository;
import com.ilanacpa.backend.user.UserService;
import com.ilanacpa.backend.user.UserStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;

    public AuthService(UserRepository userRepository, UserService userService, PasswordEncoder passwordEncoder,
                        JwtService jwtService, RefreshTokenService refreshTokenService, AuditService auditService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.auditService = auditService;
    }

    public TokenPairResponse login(String email, String rawPassword, String ipAddress) {
        User user = userService.findByEmail(email).orElse(null);

        if (user == null || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            auditService.log(AuditAction.LOGIN_FAILURE, null, ipAddress);
            throw new BadCredentialsException("Invalid email or password");
        }
        if (user.getStatus() != UserStatus.active) {
            auditService.log(AuditAction.LOGIN_FAILURE, user.getId(), ipAddress);
            throw new BadCredentialsException("Invalid email or password");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.issue(user.getId());
        auditService.log(AuditAction.LOGIN_SUCCESS, user.getId(), ipAddress);

        return new TokenPairResponse(accessToken, refreshToken, UserSummary.from(user));
    }

    public TokenPairResponse refresh(String presentedRefreshToken, String ipAddress) {
        RefreshTokenService.RotationResult result;
        try {
            result = refreshTokenService.rotate(presentedRefreshToken);
        } catch (RefreshTokenReuseException ex) {
            auditService.log(AuditAction.REFRESH_REUSE_DETECTED, ex.getUserId(), ipAddress);
            throw new BadCredentialsException("Refresh token reuse detected — please sign in again");
        } catch (IllegalArgumentException ex) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        User user = userRepository.findById(result.userId()).orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getStatus() != UserStatus.active) {
            refreshTokenService.revokeAllForUser(user.getId());
            throw new BadCredentialsException("Account is not active");
        }

        String accessToken = jwtService.generateAccessToken(user);
        return new TokenPairResponse(accessToken, result.rawToken(), UserSummary.from(user));
    }

    public void logout(String refreshToken) {
        refreshTokenService.revokeFamilyOf(refreshToken);
    }
}
