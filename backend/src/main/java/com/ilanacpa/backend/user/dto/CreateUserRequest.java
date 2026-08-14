package com.ilanacpa.backend.user.dto;

import com.ilanacpa.backend.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @NotBlank @Email String email,
        String fullName,
        @NotNull Role role) {
}
