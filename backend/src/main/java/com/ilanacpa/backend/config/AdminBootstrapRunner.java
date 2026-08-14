package com.ilanacpa.backend.config;

import com.ilanacpa.backend.user.Role;
import com.ilanacpa.backend.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Creates the first admin account on startup if it doesn't exist yet, from
 * ADMIN_EMAIL / ADMIN_PASSWORD env vars. No-op if either is unset or the
 * account already exists — there's no "promote to admin" UI since there's
 * only ever meant to be one admin created this way.
 */
@Component
@EnableConfigurationProperties(AdminBootstrapProperties.class)
public class AdminBootstrapRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final AdminBootstrapProperties properties;
    private final UserService userService;

    public AdminBootstrapRunner(AdminBootstrapProperties properties, UserService userService) {
        this.properties = properties;
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        String email = properties.email();
        String password = properties.password();
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return;
        }
        if (userService.findByEmail(email).isPresent()) {
            return;
        }
        userService.createUser(email, null, Role.ADMIN, password);
        log.info("Bootstrapped admin account for {}", email);
    }
}
