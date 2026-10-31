package com.bankwise.security.oauth2.info;

import org.springframework.util.StringUtils;

import java.util.Map;

public class GoogleOAuth2UserInfo extends OAuth2UserInfo {

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("picture");
    }

    @Override
    public String getFirstName() {
        String givenName = (String) attributes.get("given_name");
        if (StringUtils.hasText(givenName)) {
            return givenName.trim();
        }
        String name = getName();
        if (StringUtils.hasText(name)) {
            String[] parts = name.trim().split("\\s+", 2);
            return parts[0];
        }
        return "Google";
    }

    @Override
    public String getLastName() {
        String familyName = (String) attributes.get("family_name");
        if (StringUtils.hasText(familyName)) {
            return familyName.trim();
        }
        String name = getName();
        if (StringUtils.hasText(name)) {
            String[] parts = name.trim().split("\\s+", 2);
            if (parts.length > 1) {
                return parts[1];
            }
        }
        return "User";
    }
}

