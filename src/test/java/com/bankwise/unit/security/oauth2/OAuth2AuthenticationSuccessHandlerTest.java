package com.bankwise.unit.security.oauth2;

import com.bankwise.auth.domain.User;
import com.bankwise.common.exception.BusinessException;
import com.bankwise.security.jwt.JwtService;
import com.bankwise.security.oauth2.config.OAuth2Properties;
import com.bankwise.security.oauth2.handler.OAuth2AuthenticationSuccessHandler;
import com.bankwise.security.oauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import com.bankwise.security.oauth2.user.CustomOAuth2UserPrincipal;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    private OAuth2Properties oAuth2Properties;
    private OAuth2AuthenticationSuccessHandler successHandler;

    @BeforeEach
    void setUp() {
        oAuth2Properties = new OAuth2Properties();
        oAuth2Properties.setAuthorizedRedirectUris(List.of(
                "http://localhost:3000/oauth2/redirect",
                "http://localhost:5173/oauth2/redirect"
        ));
        oAuth2Properties.setDefaultRedirectUri("http://localhost:3000/oauth2/redirect");

        successHandler = new OAuth2AuthenticationSuccessHandler(
                jwtService,
                oAuth2Properties,
                authorizationRequestRepository
        );
    }

    @Test
    void testRedirectsToDefaultUriWithToken() throws IOException, ServletException {
        User user = new User();
        user.setEmail("user@example.com");
        CustomOAuth2UserPrincipal principal = new CustomOAuth2UserPrincipal(user, Map.of("email", "user@example.com"));

        when(authentication.getPrincipal()).thenReturn(principal);
        when(jwtService.generateToken(principal)).thenReturn("mocked-jwt-token-xyz");
        when(request.getCookies()).thenReturn(new Cookie[0]);
        when(response.encodeRedirectURL(any())).thenAnswer(invocation -> invocation.getArgument(0));

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect("http://localhost:3000/oauth2/redirect?token=mocked-jwt-token-xyz");
        verify(authorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
    }

    @Test
    void testRedirectsToCustomAuthorizedUriFromCookie() throws IOException, ServletException {
        User user = new User();
        user.setEmail("user@example.com");
        CustomOAuth2UserPrincipal principal = new CustomOAuth2UserPrincipal(user, Map.of("email", "user@example.com"));

        Cookie redirectCookie = new Cookie(
                HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME,
                "http://localhost:5173/oauth2/redirect"
        );

        when(authentication.getPrincipal()).thenReturn(principal);
        when(jwtService.generateToken(principal)).thenReturn("mocked-jwt-token-5173");
        when(request.getCookies()).thenReturn(new Cookie[]{redirectCookie});
        when(response.encodeRedirectURL(any())).thenAnswer(invocation -> invocation.getArgument(0));

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect("http://localhost:5173/oauth2/redirect?token=mocked-jwt-token-5173");
        verify(authorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
    }

    @Test
    void testUnauthorizedRedirectUriThrowsBusinessException() {
        User user = new User();
        user.setEmail("user@example.com");
        CustomOAuth2UserPrincipal principal = new CustomOAuth2UserPrincipal(user, Map.of("email", "user@example.com"));

        Cookie maliciousCookie = new Cookie(
                HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME,
                "https://malicious-site.com/steal-token"
        );

        when(request.getCookies()).thenReturn(new Cookie[]{maliciousCookie});

        assertThrows(BusinessException.class, () ->
                successHandler.onAuthenticationSuccess(request, response, authentication));
    }
}

