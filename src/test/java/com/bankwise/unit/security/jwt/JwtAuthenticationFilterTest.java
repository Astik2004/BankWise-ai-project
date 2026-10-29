package com.bankwise.unit.security.jwt;

import com.bankwise.auth.service.TokenRevocationService;
import com.bankwise.security.jwt.JwtAuthenticationFilter;
import com.bankwise.security.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private TokenRevocationService tokenRevocationService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        lenient().when(request.getHeader("X-Correlation-Id")).thenReturn("test-correlation-id");
        lenient().when(response.isCommitted()).thenReturn(false);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testValidTokenSetsAuthentication() throws Exception {
        String token = "valid-token";
        String email = "user@bankwise.com";
        String tokenId = "token-123";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn(email);
        when(jwtService.extractTokenId(token)).thenReturn(tokenId);
        when(tokenRevocationService.isRevoked(tokenId)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);
        doReturn(Collections.emptyList()).when(userDetails).getAuthorities();

        invokeDoFilter(filter, request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(userDetails, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testRevokedTokenDoesNotSetAuthentication() throws Exception {
        String token = "revoked-token";
        String email = "user@bankwise.com";
        String tokenId = "token-revoked-123";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn(email);
        when(jwtService.extractTokenId(token)).thenReturn(tokenId);
        when(tokenRevocationService.isRevoked(tokenId)).thenReturn(true);

        invokeDoFilter(filter, request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testNoAuthorizationHeaderContinuesChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        invokeDoFilter(filter, request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    private void invokeDoFilter(JwtAuthenticationFilter filter, HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws Exception {
        Method method = JwtAuthenticationFilter.class.getDeclaredMethod("doFilterInternal", HttpServletRequest.class, HttpServletResponse.class, FilterChain.class);
        method.setAccessible(true);
        method.invoke(filter, req, res, chain);
    }
}
