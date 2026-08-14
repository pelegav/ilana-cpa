package com.ilanacpa.backend.auth;

import com.ilanacpa.backend.auth.dto.LoginRequest;
import com.ilanacpa.backend.auth.dto.RefreshRequest;
import com.ilanacpa.backend.auth.dto.TokenPairResponse;
import com.ilanacpa.backend.auth.dto.UserSummary;
import com.ilanacpa.backend.common.NotFoundException;
import com.ilanacpa.backend.common.RateLimitExceededException;
import com.ilanacpa.backend.config.RateLimitService;
import com.ilanacpa.backend.security.UserPrincipal;
import com.ilanacpa.backend.user.User;
import com.ilanacpa.backend.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final RateLimitService rateLimitService;

    public AuthController(AuthService authService, UserRepository userRepository, RateLimitService rateLimitService) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/login")
    public TokenPairResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        if (!rateLimitService.tryConsumeLoginByIp(ip) || !rateLimitService.tryConsumeLoginByAccount(request.email())) {
            throw new RateLimitExceededException("Too many login attempts — please try again later");
        }
        return authService.login(request.email(), request.password(), ip);
    }

    @PostMapping("/refresh")
    public TokenPairResponse refresh(@Valid @RequestBody RefreshRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        if (!rateLimitService.tryConsumeRefreshByIp(ip)) {
            throw new RateLimitExceededException("Too many refresh attempts — please try again later");
        }
        return authService.refresh(request.refreshToken(), ip);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UserSummary me(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.getId()).orElseThrow(() -> new NotFoundException("User not found"));
        return UserSummary.from(user);
    }
}
