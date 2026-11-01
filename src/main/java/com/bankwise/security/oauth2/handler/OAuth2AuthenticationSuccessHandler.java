package com.bankwise.security.oauth2.handler;

import com.bankwise.auth.domain.Role;
import com.bankwise.security.jwt.JwtService;
import com.bankwise.security.oauth2.config.OAuth2Properties;
import com.bankwise.security.oauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import com.bankwise.security.oauth2.user.CustomOAuth2UserPrincipal;
import com.bankwise.security.oauth2.util.CookieUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final OAuth2Properties oAuth2Properties;
    private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect to {}", targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {
        Optional<String> redirectUriOpt = CookieUtils.getCookie(
                request,
                HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME).map(Cookie::getValue);

        String targetUrl = oAuth2Properties.getDefaultRedirectUri();

        if (redirectUriOpt.isPresent()) {
            String rawUri = redirectUriOpt.get();
            String cleanedUri = cleanUri(rawUri);

            if (isAuthorizedRedirectUri(cleanedUri)) {
                targetUrl = cleanedUri;
            } else {
                log.warn("Redirect URI '{}' was not authorized. Falling back safely to default: '{}'",
                        cleanedUri, oAuth2Properties.getDefaultRedirectUri());
                targetUrl = oAuth2Properties.getDefaultRedirectUri();
            }
        }

        log.info("Redirecting authenticated OAuth2 user to: {}", targetUrl);

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomOAuth2UserPrincipal userPrincipal)) {
            log.error("Unsupported authentication principal: {}", principal);
            return UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("error", "Unsupported principal")
                    .build().toUriString();
        }

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userPrincipal.getUserId().toString());
        if (userPrincipal.getUser().getRoles() != null) {
            extraClaims.put("roles", userPrincipal.getUser().getRoles().stream().map(Role::name).toList());
        }

        String firstName = userPrincipal.getUser().getFirstName() != null ? userPrincipal.getUser().getFirstName() : "";
        String lastName = userPrincipal.getUser().getLastName() != null ? userPrincipal.getUser().getLastName() : "";
        String fullName = (firstName + " " + lastName).trim();
        if (!fullName.isEmpty()) {
            extraClaims.put("name", fullName);
        }

        String avatarUrl = userPrincipal.getUser().getAvatarUrl();
        if (!StringUtils.hasText(avatarUrl)) {
            Map<String, Object> attrs = userPrincipal.getAttributes();
            if (attrs != null) {
                if (attrs.get("picture") instanceof String pic && StringUtils.hasText(pic)) {
                    avatarUrl = pic;
                } else if (attrs.get("avatar_url") instanceof String pic && StringUtils.hasText(pic)) {
                    avatarUrl = pic;
                }
            }
        }
        if (StringUtils.hasText(avatarUrl)) {
            extraClaims.put("avatarUrl", avatarUrl);
            extraClaims.put("avatar", avatarUrl);
            extraClaims.put("picture", avatarUrl);
        }

        String token = jwtService.generateToken(extraClaims, userPrincipal);

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", token)
                .build().toUriString();
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    private String cleanUri(String uri) {
        if (!StringUtils.hasText(uri)) {
            return "";
        }
        String cleaned = uri.trim();
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length() > 1) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }
        try {
            cleaned = URLDecoder.decode(cleaned, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        }
        return cleaned;
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        String cleanedUri = cleanUri(uri);
        if (cleanedUri.isEmpty()) {
            return false;
        }

        URI clientRedirectUri;
        try {
            clientRedirectUri = URI.create(cleanedUri);
        } catch (Exception e) {
            log.warn("Malformed redirect URI syntax: '{}'", cleanedUri);
            return false;
        }

        String clientHost = clientRedirectUri.getHost();
        int clientPort = clientRedirectUri.getPort();
        String clientPath = clientRedirectUri.getPath();

        // 1. Automatically authorize localhost and 127.0.0.1 for local frontend dev
        if (isLocalHost(clientHost)) {
            if (clientPath != null && clientPath.contains("/oauth2/redirect")) {
                return true;
            }
        }

        // 2. Validate against configured authorizedRedirectUris
        List<String> authorizedUris = oAuth2Properties.getAuthorizedRedirectUris();
        if (authorizedUris == null || authorizedUris.isEmpty()) {
            return isLocalHost(clientHost);
        }

        return authorizedUris.stream().anyMatch(authorizedRedirectUri -> {
            try {
                URI authUri = URI.create(cleanUri(authorizedRedirectUri));
                boolean hostMatches = (authUri.getHost() != null && clientHost != null) &&
                        (authUri.getHost().equalsIgnoreCase(clientHost)
                                || (isLocalHost(authUri.getHost()) && isLocalHost(clientHost)));

                int authPort = authUri.getPort();
                boolean portMatches = (authPort == clientPort)
                        || (isLocalHost(clientHost) && (clientPort == 5173 || clientPort == 5174 || clientPort == 3000
                                || clientPort == -1));

                return hostMatches && portMatches;
            } catch (Exception e) {
                return false;
            }
        });
    }

    private boolean isLocalHost(String host) {
        return host != null && (
                host.equalsIgnoreCase("localhost")
                || host.equals("127.0.0.1")
                || host.equals("::1")
        );
    }
}
