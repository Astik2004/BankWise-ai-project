package com.bankwise.security.jwt;

import com.bankwise.auth.service.TokenRevocationService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final Pattern SAFE_CORRELATION_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,64}$");

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRevocationService tokenRevocationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            setCorrelationId(request, response);
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);

            if (!StringUtils.hasText(authHeader)
                    || !authHeader.startsWith(BEARER_PREFIX)) {

                filterChain.doFilter(request, response);
                return;
            }

            String jwt = authHeader.substring(BEARER_PREFIX.length()).trim();

            if (!StringUtils.hasText(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            String userEmail;
            String tokenId;
            try {
                userEmail = jwtService.extractUsername(jwt);
                tokenId = jwtService.extractTokenId(jwt);
            } catch (JwtException | IllegalArgumentException exception) {
                log.debug("JWT validation/extraction failed: {}", exception.getMessage());
                filterChain.doFilter(request, response);
                return;
            }

            if (StringUtils.hasText(tokenId) && tokenRevocationService.isRevoked(tokenId)) {
                log.warn("Blocked request with revoked token: tokenId={}", tokenId);
                filterChain.doFilter(request, response);
                return;
            }

            if (StringUtils.hasText(userEmail)
                    && SecurityContextHolder.getContext()
                    .getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);

                    MDC.put("user", userEmail);

                    log.debug(
                            "JWT authentication successful for user: {}",
                            userEmail
                    );
                }
            }

            filterChain.doFilter(request, response);

        } finally {
            MDC.clear();
        }
    }

    private void setCorrelationId(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String correlationId = null;
        if (request != null) {
            correlationId = request.getHeader(CORRELATION_ID_HEADER);
        }

        if (!StringUtils.hasText(correlationId) || !SAFE_CORRELATION_ID_PATTERN.matcher(correlationId).matches()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put("correlationId", correlationId);

        if (response != null && !response.isCommitted()) {
            response.setHeader(CORRELATION_ID_HEADER, correlationId);
        }
    }
}
