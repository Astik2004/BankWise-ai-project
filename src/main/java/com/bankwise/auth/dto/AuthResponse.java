package com.bankwise.auth.dto;

public record AuthResponse(
        String accessToken,
        String tokenType
) {
}