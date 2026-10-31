package com.bankwise.unit.security.oauth2;

import com.bankwise.auth.domain.AccountStatus;
import com.bankwise.auth.domain.AuthProvider;
import com.bankwise.auth.domain.Role;
import com.bankwise.auth.domain.User;
import com.bankwise.auth.repository.UserRepository;
import com.bankwise.security.oauth2.exception.OAuth2AuthenticationProcessingException;
import com.bankwise.security.oauth2.info.GoogleOAuth2UserInfo;
import com.bankwise.security.oauth2.service.OAuth2UserProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2UserProcessingServiceTest {

    @Mock
    private UserRepository userRepository;

    private OAuth2UserProcessingService processingService;

    @BeforeEach
    void setUp() {
        processingService = new OAuth2UserProcessingService(userRepository);
    }

    @Test
    void testRegisterNewUserSuccess() {
        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(Map.of(
                "sub", "google-id-100",
                "email", "newuser@example.com",
                "name", "New User",
                "given_name", "New",
                "family_name", "User",
                "picture", "https://example.com/photo.png"
        ));

        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = processingService.processOAuth2User("google", userInfo);

        assertNotNull(result);
        assertEquals("newuser@example.com", result.getEmail());
        assertEquals("New", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals(AuthProvider.GOOGLE, result.getAuthProvider());
        assertEquals("google-id-100", result.getProviderId());
        assertEquals("https://example.com/photo.png", result.getAvatarUrl());
        assertEquals(AccountStatus.ACTIVE, result.getAccountStatus());
        assertTrue(result.isEmailVerified());
        assertEquals(Set.of(Role.USER), result.getRoles());
        assertNotNull(result.getLastLoginAt());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateExistingUserSuccess() {
        User existingUser = new User();
        existingUser.setEmail("existing@example.com");
        existingUser.setFirstName("OldFirst");
        existingUser.setLastName("OldLast");
        existingUser.setAuthProvider(AuthProvider.LOCAL);
        existingUser.setAccountStatus(AccountStatus.ACTIVE);
        existingUser.setEmailVerified(false);
        existingUser.setFailedLoginAttempts(3);

        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(Map.of(
                "sub", "google-id-200",
                "email", "existing@example.com",
                "name", "OldFirst OldLast",
                "picture", "https://example.com/newphoto.png"
        ));

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = processingService.processOAuth2User("google", userInfo);

        assertEquals("google-id-200", result.getProviderId());
        assertEquals(AuthProvider.GOOGLE, result.getAuthProvider());
        assertEquals("https://example.com/newphoto.png", result.getAvatarUrl());
        assertTrue(result.isEmailVerified());
        assertEquals(0, result.getFailedLoginAttempts());
        assertNull(result.getLockedUntil());
        assertNotNull(result.getLastLoginAt());
    }

    @Test
    void testLockedUserThrowsOAuth2Exception() {
        User lockedUser = new User();
        lockedUser.setEmail("locked@example.com");
        lockedUser.setAccountStatus(AccountStatus.LOCKED);

        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(Map.of(
                "sub", "google-id-300",
                "email", "locked@example.com"
        ));

        when(userRepository.findByEmail("locked@example.com")).thenReturn(Optional.of(lockedUser));

        OAuth2AuthenticationProcessingException ex = assertThrows(
                OAuth2AuthenticationProcessingException.class,
                () -> processingService.processOAuth2User("google", userInfo)
        );

        assertTrue(ex.getMessage().contains("locked"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testMissingEmailThrowsException() {
        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(Map.of(
                "sub", "google-id-400"
        ));

        assertThrows(
                OAuth2AuthenticationProcessingException.class,
                () -> processingService.processOAuth2User("google", userInfo)
        );
    }

    @Test
    void testMissingProviderIdThrowsException() {
        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(Map.of(
                "email", "user@example.com"
        ));

        assertThrows(
                OAuth2AuthenticationProcessingException.class,
                () -> processingService.processOAuth2User("google", userInfo)
        );
    }
}

