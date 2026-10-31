package com.bankwise.unit.security.oauth2;

import com.bankwise.security.oauth2.config.OAuth2Properties;
import com.bankwise.security.oauth2.handler.OAuth2AuthenticationFailureHandler;
import com.bankwise.security.oauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationFailureHandlerTest {

    @Mock
    private HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private OAuth2Properties oAuth2Properties;
    private OAuth2AuthenticationFailureHandler failureHandler;

    @BeforeEach
    void setUp() {
        oAuth2Properties = new OAuth2Properties();
        oAuth2Properties.setDefaultRedirectUri("http://localhost:3000/oauth2/redirect");

        failureHandler = new OAuth2AuthenticationFailureHandler(
                authorizationRequestRepository,
                oAuth2Properties
        );
    }

    @Test
    void testRedirectsWithErrorMessage() throws IOException, ServletException {
        when(request.getCookies()).thenReturn(new Cookie[0]);
        when(response.encodeRedirectURL(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AuthenticationException exception = new BadCredentialsException("OAuth login failed");

        failureHandler.onAuthenticationFailure(request, response, exception);

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());

        String redirectUrl = redirectCaptor.getValue();
        assertTrue(redirectUrl.startsWith("http://localhost:3000/oauth2/redirect?error="));
        assertTrue(redirectUrl.contains("OAuth") && redirectUrl.contains("failed"));

        verify(authorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
    }
}
