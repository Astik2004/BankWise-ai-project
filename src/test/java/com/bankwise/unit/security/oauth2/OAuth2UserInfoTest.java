package com.bankwise.unit.security.oauth2;

import com.bankwise.common.exception.BusinessException;
import com.bankwise.security.oauth2.info.GithubOAuth2UserInfo;
import com.bankwise.security.oauth2.info.GoogleOAuth2UserInfo;
import com.bankwise.security.oauth2.info.OAuth2UserInfo;
import com.bankwise.security.oauth2.info.OAuth2UserInfoFactory;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OAuth2UserInfoTest {

    @Test
    void testGoogleOAuth2UserInfoParsing() {
        Map<String, Object> attributes = Map.of(
                "sub", "google-12345",
                "email", "alice@gmail.com",
                "name", "Alice Smith",
                "given_name", "Alice",
                "family_name", "Smith",
                "picture", "https://example.com/alice.jpg"
        );

        OAuth2UserInfo info = OAuth2UserInfoFactory.getOAuth2UserInfo("google", attributes);

        assertInstanceOf(GoogleOAuth2UserInfo.class, info);
        assertEquals("google-12345", info.getId());
        assertEquals("alice@gmail.com", info.getEmail());
        assertEquals("Alice Smith", info.getName());
        assertEquals("Alice", info.getFirstName());
        assertEquals("Smith", info.getLastName());
        assertEquals("https://example.com/alice.jpg", info.getImageUrl());
    }

    @Test
    void testGithubOAuth2UserInfoParsing() {
        Map<String, Object> attributes = Map.of(
                "id", 98765,
                "login", "bobgit",
                "email", "bob@example.com",
                "name", "Bob Johnson",
                "avatar_url", "https://example.com/bob.jpg"
        );

        OAuth2UserInfo info = OAuth2UserInfoFactory.getOAuth2UserInfo("github", attributes);

        assertInstanceOf(GithubOAuth2UserInfo.class, info);
        assertEquals("98765", info.getId());
        assertEquals("bob@example.com", info.getEmail());
        assertEquals("Bob Johnson", info.getName());
        assertEquals("Bob", info.getFirstName());
        assertEquals("Johnson", info.getLastName());
        assertEquals("https://example.com/bob.jpg", info.getImageUrl());
    }

    @Test
    void testGithubOAuth2UserInfoFallbackToLoginWhenNameEmpty() {
        Map<String, Object> attributes = Map.of(
                "id", 11223,
                "login", "developer1"
        );

        OAuth2UserInfo info = OAuth2UserInfoFactory.getOAuth2UserInfo("github", attributes);

        assertEquals("developer1", info.getFirstName());
        assertEquals("User", info.getLastName());
    }

    @Test
    void testUnsupportedProviderThrowsBusinessException() {
        assertThrows(BusinessException.class, () ->
                OAuth2UserInfoFactory.getOAuth2UserInfo("facebook", Map.of()));
    }

    @Test
    void testNullProviderThrowsBusinessException() {
        assertThrows(BusinessException.class, () ->
                OAuth2UserInfoFactory.getOAuth2UserInfo(null, Map.of()));
    }
}

