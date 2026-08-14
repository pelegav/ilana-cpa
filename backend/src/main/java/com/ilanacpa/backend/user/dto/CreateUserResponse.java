package com.ilanacpa.backend.user.dto;

import com.ilanacpa.backend.auth.dto.UserSummary;

public record CreateUserResponse(UserSummary user, String temporaryPassword) {
}
