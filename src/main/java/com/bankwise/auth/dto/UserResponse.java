package com.bankwise.auth.dto;

import com.bankwise.auth.domain.AccountStatus;
import com.bankwise.auth.domain.Role;

import java.time.Instant;
import java.util.Set;

public record UserResponse(
        String id,
        String email,
        String firstName,
        String lastName,
        Set<Role> roles,
        AccountStatus status,
        boolean emailVerified,
        Instant createdAt
) {
}