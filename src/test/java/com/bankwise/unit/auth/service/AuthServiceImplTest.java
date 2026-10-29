package com.bankwise.unit.auth.service;

import com.bankwise.auth.domain.AccountStatus;
import com.bankwise.auth.domain.Role;
import com.bankwise.auth.domain.User;
import com.bankwise.auth.dto.AuthResponse;
import com.bankwise.auth.dto.ChangePasswordRequest;
import com.bankwise.auth.dto.LoginRequest;
import com.bankwise.auth.dto.RegisterRequest;
import com.bankwise.auth.repository.UserRepository;
import com.bankwise.auth.service.AuthServiceImpl;
import com.bankwise.auth.service.TokenRevocationService;
import com.bankwise.common.exception.BusinessException;
import com.bankwise.common.exception.ResourceNotFoundException;
import com.bankwise.security.jwt.JwtService;
import com.bankwise.security.principal.CustomUserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenRevocationService tokenRevocationService;

    @InjectMocks
    private AuthServiceImpl authServiceImpl;

    @Test
    void testRegisterUser() {

        RegisterRequest request = new RegisterRequest("Astik",
                "Yadav",
                "astik@gmail.com",
                "astik@123"
        );

        when(userRepository.existsByEmail("astik@gmail.com")).thenReturn(false);

        when(passwordEncoder.encode("astik@123")).thenReturn("encodedpassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authServiceImpl.register(request);

        assertNotNull(response);

        assertNull(response.accessToken());

        assertEquals("Bearer", response.tokenType());
    }

    @Test
    void testLoginUser() {

        LoginRequest request = new LoginRequest(
                "astik@gmail.com",
                "astik@123"
        );

        User user = new User();
        user.setEmail("astik@gmail.com");
        user.setPasswordHash("encoded-password");
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setFailedLoginAttempts(0);
        user.setRoles(Set.of(Role.USER));

        when(userRepository.findByEmail("astik@gmail.com")).thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "astik@123",
                "encoded-password"
        )).thenReturn(true);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(jwtService.generateToken(any(CustomUserPrincipal.class)))
                .thenReturn("jwt-access-token");

        AuthResponse response = authServiceImpl.login(request);

        assertNotNull(response);

        assertEquals("jwt-access-token", response.accessToken());

        assertEquals("Bearer", response.tokenType());
    }

    @Test
    void testLogoutUser() {

        String token = "jwt-token";
        String authHeader = "Bearer " + token;
        Instant expirationTime = Instant.now().plusSeconds(3600);

        when(jwtService.isTokenValid(token)).thenReturn(true);

        when(jwtService.extractTokenId(token)).thenReturn("token-123");

        when(jwtService.extractExpiration(token))
                .thenReturn(expirationTime);

        authServiceImpl.logout(authHeader);
    }

    @Test
    void testChangePasswordSuccess() {
        String email = "astik@gmail.com";
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword123", "newPassword123");

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("encoded-old-password");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword123", "encoded-old-password")).thenReturn(true);
        when(passwordEncoder.matches("newPassword123", "encoded-old-password")).thenReturn(false);
        when(passwordEncoder.encode("newPassword123")).thenReturn("encoded-new-password");

        authServiceImpl.changePassword(email, request);

        assertEquals("encoded-new-password", user.getPasswordHash());
        assertNotNull(user.getPasswordChangedAt());
        verify(userRepository).save(user);
    }

    @Test
    void testChangePasswordInvalidCurrentPasswordThrowsBadCredentials() {
        String email = "astik@gmail.com";
        ChangePasswordRequest request = new ChangePasswordRequest("wrongOldPassword", "newPassword123");

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("encoded-old-password");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOldPassword", "encoded-old-password")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authServiceImpl.changePassword(email, request));
    }

    @Test
    void testChangePasswordSameAsOldThrowsBusinessException() {
        String email = "astik@gmail.com";
        ChangePasswordRequest request = new ChangePasswordRequest("samePassword123", "samePassword123");

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("encoded-password");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("samePassword123", "encoded-password")).thenReturn(true);

        assertThrows(BusinessException.class, () -> authServiceImpl.changePassword(email, request));
    }

    @Test
    void testChangePasswordUserNotFoundThrowsResourceNotFound() {
        String email = "unknown@gmail.com";
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword123", "newPassword123");

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authServiceImpl.changePassword(email, request));
    }
}
