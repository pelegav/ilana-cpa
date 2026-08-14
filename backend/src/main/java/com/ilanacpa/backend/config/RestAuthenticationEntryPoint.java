package com.ilanacpa.backend.config;

import com.ilanacpa.backend.common.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * By default, Spring Security's anonymous-authentication support means a request with
 * no/invalid credentials is denied with 403 (AccessDeniedException), the same as a
 * logged-in user lacking a role. This entry point restores the usual REST convention:
 * 401 for "who are you", 403 (via GlobalExceptionHandler's AccessDeniedException handler)
 * reserved for "I know who you are, but no".
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws java.io.IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        ApiError body = new ApiError(Instant.now(), HttpStatus.UNAUTHORIZED.value(), "Unauthorized",
                "Authentication required", request.getRequestURI());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
