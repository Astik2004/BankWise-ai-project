package com.bankwise.security.oauth2.service;

import com.bankwise.auth.domain.User;
import com.bankwise.security.oauth2.info.OAuth2UserInfo;
import com.bankwise.security.oauth2.info.OAuth2UserInfoFactory;
import com.bankwise.security.oauth2.user.CustomOAuth2UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final String GITHUB_REGISTRATION_ID = "github";
    private static final String GITHUB_EMAILS_URL = "https://api.github.com/user/emails";

    private final OAuth2UserProcessingService userProcessingService;
    private final RestClient restClient = RestClient.create();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

        if (GITHUB_REGISTRATION_ID.equalsIgnoreCase(registrationId)
                && !StringUtils.hasText((String) attributes.get("email"))) {
            String resolvedEmail = fetchGithubPrimaryEmail(userRequest.getAccessToken().getTokenValue(), attributes);
            if (StringUtils.hasText(resolvedEmail)) {
                attributes.put("email", resolvedEmail);
            }
        }

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);
        User user = userProcessingService.processOAuth2User(registrationId, userInfo);

        return new CustomOAuth2UserPrincipal(user, attributes);
    }

    private String fetchGithubPrimaryEmail(String accessToken, Map<String, Object> attributes) {
        try {
            List<Map<String, Object>> emails = restClient.get()
                    .uri(GITHUB_EMAILS_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/vnd.github+json")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            if (emails != null) {
                for (Map<String, Object> emailEntry : emails) {
                    Boolean primary = (Boolean) emailEntry.get("primary");
                    Boolean verified = (Boolean) emailEntry.get("verified");
                    String email = (String) emailEntry.get("email");

                    if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified) && StringUtils.hasText(email)) {
                        return email.trim();
                    }
                }

                for (Map<String, Object> emailEntry : emails) {
                    String email = (String) emailEntry.get("email");
                    if (StringUtils.hasText(email)) {
                        return email.trim();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch emails from GitHub API: {}", e.getMessage());
        }

        Object id = attributes.get("id");
        Object login = attributes.get("login");
        if (id != null && login != null) {
            return id + "+" + login + "@users.noreply.github.com";
        }
        return null;
    }
}

