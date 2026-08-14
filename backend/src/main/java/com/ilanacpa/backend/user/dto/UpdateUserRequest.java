package com.ilanacpa.backend.user.dto;

import com.ilanacpa.backend.user.Role;
import com.ilanacpa.backend.user.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(@NotNull Role role, @NotNull UserStatus status) {
}
