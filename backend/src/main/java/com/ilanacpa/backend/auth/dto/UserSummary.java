package com.ilanacpa.backend.auth.dto;

import com.ilanacpa.backend.user.Role;
import com.ilanacpa.backend.user.User;
import java.util.UUID;

public record UserSummary(UUID id, String email, String fullName, Role role) {
    public static UserSummary from(User user) {
        return new UserSummary(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }
}
