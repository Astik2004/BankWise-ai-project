package com.bankwise.auth.service;

import com.bankwise.auth.domain.AccountStatus;
import com.bankwise.auth.domain.Role;
import com.bankwise.auth.domain.User;
import com.bankwise.auth.dto.AuthResponse;
import com.bankwise.auth.dto.ChangePasswordRequest;
import com.bankwise.auth.dto.LoginRequest;
import com.bankwise.auth.dto.RegisterRequest;
import com.bankwise.auth.repository.UserRepository;
import com.bankwise.common.exception.BusinessException;
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
import java.util.Locale;
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
            throw new BusinessException("Email already exists");
        }
        User user = buildUser(request, email);
        try {
            User savedUser = userRepository.save(user);

            log.info("User registered successfully. userId={}", savedUser.getId());

            return new AuthResponse(null, BEARER_TOKEN);

        } catch (DuplicateKeyException exception) {
            log.warn("Duplicate email registration attempt. email={}", email);
            throw new BusinessException("Email already exists");
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
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            handleFailedLogin(user);
            throw invalidCredentials();
        }

        handleSuccessfulLogin(user);

        CustomUserPrincipal principal = CustomUserPrincipal.fromUser(user);
        String accessToken = jwtService.generateToken(principal);

        log.info("User logged in successfully. userId={}", user.getId());

        return new AuthResponse(accessToken, BEARER_TOKEN);
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
        throw new UnsupportedOperationException("Change password is not implemented in Auth V1");
    }

    private User buildUser(RegisterRequest request, String email) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(normalizeName(request.firstName(), "First name"));
        user.setLastName(normalizeName(request.lastName(), "Last name"));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(Role.USER));
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setEmailVerified(false);
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

        if (!user.isEmailVerified()) {
            throw new BusinessException("Email verification is required");
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