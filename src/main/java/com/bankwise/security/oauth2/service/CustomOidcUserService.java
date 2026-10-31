package com.bankwise.security.oauth2.service;

import com.bankwise.auth.domain.User;
import com.bankwise.security.oauth2.info.OAuth2UserInfo;
import com.bankwise.security.oauth2.info.OAuth2UserInfoFactory;
import com.bankwise.security.oauth2.user.CustomOAuth2UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final OAuth2UserProcessingService userProcessingService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        Map<String, Object> attributes = oidcUser.getAttributes();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);
        User user = userProcessingService.processOAuth2User(registrationId, userInfo);

        return new CustomOAuth2UserPrincipal(user, attributes, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }
}

