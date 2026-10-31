package com.bankwise.security.oauth2.info;

import org.springframework.util.StringUtils;

import java.util.Map;

public class GithubOAuth2UserInfo extends OAuth2UserInfo {

    public GithubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        Object id = attributes.get("id");
        return id != null ? String.valueOf(id) : null;
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
        return (String) attributes.get("avatar_url");
    }

    @Override
    public String getFirstName() {
        String name = getName();
        if (StringUtils.hasText(name)) {
            String[] parts = name.trim().split("\\s+", 2);
            return parts[0];
        }
        String login = (String) attributes.get("login");
        return StringUtils.hasText(login) ? login.trim() : "GitHub";
    }

    @Override
    public String getLastName() {
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

