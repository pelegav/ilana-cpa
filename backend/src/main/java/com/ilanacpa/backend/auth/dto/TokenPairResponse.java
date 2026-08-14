package com.ilanacpa.backend.auth.dto;

public record TokenPairResponse(String accessToken, String refreshToken, UserSummary user) {
}
