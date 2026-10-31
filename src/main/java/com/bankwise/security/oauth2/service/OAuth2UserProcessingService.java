package com.bankwise.security.oauth2.service;

import com.bankwise.auth.domain.AccountStatus;
import com.bankwise.auth.domain.AuthProvider;
import com.bankwise.auth.domain.Role;
import com.bankwise.auth.domain.User;
import com.bankwise.auth.repository.UserRepository;
import com.bankwise.security.oauth2.exception.OAuth2AuthenticationProcessingException;
import com.bankwise.security.oauth2.info.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserProcessingService {

    private final UserRepository userRepository;

    public User processOAuth2User(String registrationId, OAuth2UserInfo userInfo) {
        if (!StringUtils.hasText(registrationId)) {
            throw new OAuth2AuthenticationProcessingException("OAuth2 registrationId cannot be blank");
        }

        if (userInfo == null) {
            throw new OAuth2AuthenticationProcessingException("OAuth2 user details could not be retrieved");
        }

        if (!StringUtils.hasText(userInfo.getId())) {
            throw new OAuth2AuthenticationProcessingException("Provider ID missing from OAuth2 attributes");
        }

        if (!StringUtils.hasText(userInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email address not found from OAuth2 provider");
        }

        String normalizedEmail = userInfo.getEmail().trim().toLowerCase(Locale.ROOT);
        AuthProvider provider = parseAuthProvider(registrationId);

        Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);
        User user;

        if (userOptional.isPresent()) {
            user = updateExistingUser(userOptional.get(), provider, userInfo);
        } else {
            user = registerNewUser(normalizedEmail, provider, userInfo);
        }

        return userRepository.save(user);
    }

    private User updateExistingUser(User user, AuthProvider provider, OAuth2UserInfo userInfo) {
        if (user.getAccountStatus() == AccountStatus.LOCKED) {
            throw new OAuth2AuthenticationProcessingException("Account is locked. Please contact support.");
        }

        user.setLastLoginAt(Instant.now());
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setEmailVerified(true);

        if (!StringUtils.hasText(user.getProviderId())) {
            user.setProviderId(userInfo.getId());
            user.setAuthProvider(provider);
        }

        if (!StringUtils.hasText(user.getAvatarUrl()) && StringUtils.hasText(userInfo.getImageUrl())) {
            user.setAvatarUrl(userInfo.getImageUrl());
        }

        log.info("Updated existing user from OAuth2 provider: email={}, provider={}", user.getEmail(), provider);
        return user;
    }

    private User registerNewUser(String email, AuthProvider provider, OAuth2UserInfo userInfo) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(userInfo.getFirstName());
        user.setLastName(userInfo.getLastName());
        user.setAuthProvider(provider);
        user.setProviderId(userInfo.getId());
        user.setAvatarUrl(userInfo.getImageUrl());
        user.setRoles(Set.of(Role.USER));
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setLastLoginAt(Instant.now());

        log.info("Registered new user from OAuth2 provider: email={}, provider={}", email, provider);
        return user;
    }

    private AuthProvider parseAuthProvider(String registrationId) {
        try {
            return AuthProvider.valueOf(registrationId.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new OAuth2AuthenticationProcessingException("Unsupported provider: " + registrationId);
        }
    }
}

