package com.bankwise.auth.dto;

public record LoginRequest(
        String email,
        String password
) {
}