package com.bankwise.auth.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Data
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    private String firstName;
    private String lastName;

    private Set<Role> roles;

    private AccountStatus status;

    private boolean emailVerified;

    private int failedLoginAttempts;

    private Instant lockedUntil;

    private Instant lastLoginAt;

    private Instant passwordChangedAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}