package com.bankwise.security.oauth2.info;

import com.bankwise.auth.domain.AuthProvider;
import com.bankwise.common.exception.BusinessException;

import java.util.Locale;
import java.util.Map;

public final class OAuth2UserInfoFactory {

    private OAuth2UserInfoFactory() {
    }

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId == null) {
            throw new BusinessException("OAuth2 registrationId cannot be null");
        }

        String normalized = registrationId.trim().toUpperCase(Locale.ROOT);
        if (normalized.equals(AuthProvider.GOOGLE.name())) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (normalized.equals(AuthProvider.GITHUB.name())) {
            return new GithubOAuth2UserInfo(attributes);
        } else {
            throw new BusinessException("Unsupported OAuth2 provider: " + registrationId);
        }
    }
}

