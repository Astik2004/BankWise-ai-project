package com.bankwise.unit.security.oauth2;

import com.bankwise.security.oauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import com.bankwise.security.oauth2.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpCookieOAuth2AuthorizationRequestRepositoryTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private HttpCookieOAuth2AuthorizationRequestRepository repository;

    @BeforeEach
    void setUp() {
        repository = new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    @Test
    void testSaveAuthorizationRequestAddsCookie() {
        OAuth2AuthorizationRequest authRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .clientId("test-client-id")
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .state("test-state-123")
                .build();

        when(request.getParameter(HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME))
                .thenReturn("http://localhost:3000/oauth2/redirect");

        repository.saveAuthorizationRequest(authRequest, request, response);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response, times(2)).addCookie(cookieCaptor.capture());

        var cookies = cookieCaptor.getAllValues();
        assertTrue(cookies.stream().anyMatch(c ->
                c.getName().equals(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)));
        assertTrue(cookies.stream().anyMatch(c ->
                c.getName().equals(HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)));
    }

    @Test
    void testLoadAuthorizationRequestDeserializesCookie() {
        OAuth2AuthorizationRequest authRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .clientId("test-client-id")
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .state("test-state-abc")
                .build();

        String serialized = CookieUtils.serialize(authRequest);
        Cookie cookie = new Cookie(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, serialized);

        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        OAuth2AuthorizationRequest loaded = repository.loadAuthorizationRequest(request);

        assertNotNull(loaded);
        assertEquals("test-client-id", loaded.getClientId());
        assertEquals("test-state-abc", loaded.getState());
        assertEquals("https://accounts.google.com/o/oauth2/v2/auth", loaded.getAuthorizationUri());
    }

    @Test
    void testRemoveAuthorizationRequestDeletesCookie() {
        OAuth2AuthorizationRequest authRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .clientId("test-client-id")
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .state("test-state-remove")
                .build();

        String serialized = CookieUtils.serialize(authRequest);
        Cookie cookie = new Cookie(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, serialized);

        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        OAuth2AuthorizationRequest removed = repository.removeAuthorizationRequest(request, response);

        assertNotNull(removed);
        assertEquals("test-state-remove", removed.getState());

        ArgumentCaptor<Cookie> deleteCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(deleteCaptor.capture());
        assertEquals(0, deleteCaptor.getValue().getMaxAge());
    }
}

