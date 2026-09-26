package com.bankwise.unit.auth.service;

import com.bankwise.auth.domain.AccountStatus;
import com.bankwise.auth.domain.Role;
import com.bankwise.auth.domain.User;
import com.bankwise.auth.dto.AuthResponse;
import com.bankwise.auth.dto.LoginRequest;
import com.bankwise.auth.dto.RegisterRequest;
import com.bankwise.auth.repository.UserRepository;
import com.bankwise.auth.service.AuthServiceImpl;
import com.bankwise.auth.service.TokenRevocationService;
import com.bankwise.security.jwt.JwtService;
import com.bankwise.security.principal.CustomUserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    private TokenRevocationService  tokenRevocationService;

    @InjectMocks
    private AuthServiceImpl authServiceImpl;

    @Test
    void testRegisterUser() {

        RegisterRequest request=new RegisterRequest("Astik",
                "Yadav",
                "astik@gmail.com",
                "astik@123"
        );

        when(userRepository.existsByEmail("astik@gmail.com")).thenReturn(false);

        when(passwordEncoder.encode("astik@123")).thenReturn("encodedpassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation ->  invocation.getArgument(0));

        AuthResponse response=authServiceImpl.register(request);

        assertNotNull(response);

        assertNull(response.accessToken());

        assertEquals( "Bearer",response.tokenType());
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
}
