package com.bankwise.auth.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        UserResponse user
) {
    public AuthResponse(String accessToken, String tokenType) {
        this(accessToken, tokenType, null);
    }
}
