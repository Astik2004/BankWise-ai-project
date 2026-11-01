package com.bankwise.auth.service;

import com.bankwise.auth.domain.AccountStatus;
import com.bankwise.auth.domain.Role;
import com.bankwise.auth.domain.User;
import com.bankwise.auth.dto.AuthResponse;
import com.bankwise.auth.dto.ChangePasswordRequest;
import com.bankwise.auth.dto.LoginRequest;
import com.bankwise.auth.dto.RegisterRequest;
import com.bankwise.auth.dto.UserResponse;
import com.bankwise.auth.repository.UserRepository;
import com.bankwise.common.exception.BusinessException;
import com.bankwise.common.exception.DuplicateResourceException;
import com.bankwise.common.exception.ResourceNotFoundException;
import com.bankwise.security.jwt.JwtService;
import com.bankwise.security.principal.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String BEARER_TOKEN = "Bearer";
    private static final String INVALID_CREDENTIALS = "Invalid email or password";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenRevocationService tokenRevocationService;

    @Override
    public AuthResponse register(RegisterRequest request) {

        validateRegisterRequest(request);
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists");
        }
        User user = buildUser(request, email);
        try {
            User savedUser = userRepository.save(user);

            log.info("User registered successfully. userId={}", savedUser.getId());

            CustomUserPrincipal principal = CustomUserPrincipal.fromUser(savedUser);
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("userId", savedUser.getId().toString());
            extraClaims.put("roles", savedUser.getRoles().stream().map(Role::name).toList());
            extraClaims.put("name", savedUser.getFirstName() + " " + savedUser.getLastName());
            String accessToken = jwtService.generateToken(extraClaims, principal);

            UserResponse userResponse = new UserResponse(
                    savedUser.getId().toString(),
                    savedUser.getEmail(),
                    savedUser.getFirstName(),
                    savedUser.getLastName(),
                    savedUser.getRoles(),
                    savedUser.getAccountStatus(),
                    savedUser.isEmailVerified(),
                    savedUser.getCreatedAt());

            return new AuthResponse(accessToken, BEARER_TOKEN, userResponse);

        } catch (DuplicateKeyException exception) {
            log.warn("Duplicate email registration attempt. email={}", email);
            throw new DuplicateResourceException("Email already exists");
        }
    }

    @Override
    public AuthResponse createAdmin(RegisterRequest request) {

        validateRegisterRequest(request);
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists");
        }
        User user = buildUser(request, email);
        user.setRoles(Set.of(Role.ADMIN, Role.USER));
        user.setEmailVerified(true);
        try {
            User savedUser = userRepository.save(user);

            log.info("Admin user registered successfully. userId={}", savedUser.getId());

            CustomUserPrincipal principal = CustomUserPrincipal.fromUser(savedUser);
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("userId", savedUser.getId().toString());
            extraClaims.put("roles", savedUser.getRoles().stream().map(Role::name).toList());
            extraClaims.put("name", savedUser.getFirstName() + " " + savedUser.getLastName());
            String accessToken = jwtService.generateToken(extraClaims, principal);

            UserResponse userResponse = new UserResponse(
                    savedUser.getId().toString(),
                    savedUser.getEmail(),
                    savedUser.getFirstName(),
                    savedUser.getLastName(),
                    savedUser.getRoles(),
                    savedUser.getAccountStatus(),
                    savedUser.isEmailVerified(),
                    savedUser.getCreatedAt());

            return new AuthResponse(accessToken, BEARER_TOKEN, userResponse);

        } catch (DuplicateKeyException exception) {
            log.warn("Duplicate email registration attempt. email={}", email);
            throw new DuplicateResourceException("Email already exists");
        }
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        validateLoginRequest(request);
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmail(email).orElseThrow(this::invalidCredentials);

        validateAccountStatus(user);
        boolean passwordMatches = passwordEncoder.matches(
                request.password(),
                user.getPasswordHash());

        if (!passwordMatches) {
            handleFailedLogin(user);
            throw invalidCredentials();
        }

        handleSuccessfulLogin(user);

        CustomUserPrincipal principal = CustomUserPrincipal.fromUser(user);
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId().toString());
        extraClaims.put("roles", user.getRoles().stream().map(Role::name).toList());
        extraClaims.put("name", user.getFirstName() + " " + user.getLastName());
        if (user.getAvatarUrl() != null) {
            extraClaims.put("avatarUrl", user.getAvatarUrl());
            extraClaims.put("avatar", user.getAvatarUrl());
            extraClaims.put("picture", user.getAvatarUrl());
        }
        String accessToken = jwtService.generateToken(extraClaims, principal);

        UserResponse userResponse = new UserResponse(
                user.getId().toString(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoles(),
                user.getAccountStatus(),
                user.isEmailVerified(),
                user.getCreatedAt());

        log.info("User logged in successfully. userId={}", user.getId());

        return new AuthResponse(accessToken, BEARER_TOKEN, userResponse);
    }

    @Override
    public void logout(String authHeader) {

        String token = extractBearerToken(authHeader);
        if (!jwtService.isTokenValid(token)) {
            throw new BusinessException("Invalid or expired token");
        }

        String tokenId = jwtService.extractTokenId(token);

        Instant expirationTime = jwtService.extractExpiration(token);

        if (!StringUtils.hasText(tokenId) || expirationTime == null) {
            throw new BusinessException("Invalid token claims");
        }

        tokenRevocationService.revoke(tokenId, expirationTime);
        log.info("User logout completed successfully");
    }

    @Override
    public void changePassword(String email, ChangePasswordRequest request) {
        validateChangePasswordRequest(request);

        String normalizedEmail = normalizeEmail(email);
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!StringUtils.hasText(user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
            user.setPasswordChangedAt(Instant.now());
            userRepository.save(user);
            log.info("Password set successfully for OAuth user {}", user.getId());
            return;
        }

        boolean currentPasswordMatches = passwordEncoder.matches(
                request.oldPassword(),
                user.getPasswordHash());

        if (!currentPasswordMatches) {
            throw new BusinessException("Incorrect password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);

        log.info("Password changed successfully for user {}", user.getId());
    }

    private void validateChangePasswordRequest(ChangePasswordRequest request) {
        if (request == null) {
            throw new BusinessException("Change password request is required");
        }
        if (!StringUtils.hasText(request.oldPassword())) {
            throw new BusinessException("Old password is required");
        }
        if (!StringUtils.hasText(request.newPassword())) {
            throw new BusinessException("New password is required");
        }
        if (request.newPassword().length() < 8) {
            throw new BusinessException("Password must be at least 8 characters");
        }
    }

    private User buildUser(RegisterRequest request, String email) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(normalizeName(request.firstName(), "First name"));
        user.setLastName(normalizeName(request.lastName(), "Last name"));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(Role.USER));
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(null);
        user.setPasswordChangedAt(Instant.now());
        return user;
    }

    private void validateAccountStatus(User user) {
        AccountStatus accountStatus = user.getAccountStatus();

        if (accountStatus == AccountStatus.LOCKED) {
            if (isLockExpired(user)) {
                unlockAccount(user);
            } else {
                throw new BusinessException("Account is temporarily locked");
            }
        }

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Account is inactive");
        }
    }

    private boolean isLockExpired(User user) {
        Instant lockedUntil = user.getLockedUntil();
        return lockedUntil != null && !Instant.now().isBefore(lockedUntil);
    }

    private void unlockAccount(User user) {
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
        log.info("User account unlocked. userId={}", user.getId());
    }

    private void handleFailedLogin(User user) {
        int failedAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(failedAttempts);
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            user.setAccountStatus(AccountStatus.LOCKED);
            user.setLockedUntil(Instant.now().plus(LOCK_DURATION));
            log.warn("User account locked. userId={}", user.getId());
        }
        userRepository.save(user);
        log.warn("Failed login attempt. userId={}, attempts={}", user.getId(), failedAttempts);
    }

    private void handleSuccessfulLogin(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
    }

    private String extractBearerToken(String authHeader) {
        if (!StringUtils.hasText(authHeader)) {
            throw new BusinessException("Authorization header is required");
        }
        String prefix = BEARER_TOKEN + " ";
        if (!authHeader.startsWith(prefix)) {
            throw new BusinessException("Invalid authorization header");
        }

        String token = authHeader.substring(prefix.length()).trim();
        if (!StringUtils.hasText(token)) {
            throw new BusinessException("Bearer token is missing");
        }
        return token;
    }

    private void validateRegisterRequest(RegisterRequest request) {

        if (request == null) {
            throw new BusinessException("Registration request is required");
        }

        if (!StringUtils.hasText(request.email())) {
            throw new BusinessException("Email is required");
        }

        if (!StringUtils.hasText(request.password())) {
            throw new BusinessException("Password is required");
        }

        if (!StringUtils.hasText(request.firstName())) {
            throw new BusinessException("First name is required");
        }

        if (!StringUtils.hasText(request.lastName())) {
            throw new BusinessException("Last name is required");
        }
    }

    private void validateLoginRequest(LoginRequest request) {

        if (request == null) {
            throw new BusinessException("Login request is required");
        }

        if (!StringUtils.hasText(request.email())) {
            throw new BusinessException("Email is required");
        }

        if (!StringUtils.hasText(request.password())) {
            throw new BusinessException("Password is required");
        }
    }

    private String normalizeEmail(String email) {

        if (!StringUtils.hasText(email)) {
            throw new BusinessException("Email is required");
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeName(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(fieldName + " is required");
        }
        return value.trim();
    }

    private BadCredentialsException invalidCredentials() {
        return new BadCredentialsException(INVALID_CREDENTIALS);
    }
}