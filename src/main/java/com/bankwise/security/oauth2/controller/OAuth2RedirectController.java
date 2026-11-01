package com.bankwise.security.oauth2.controller;

import com.bankwise.security.oauth2.config.OAuth2Properties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequiredArgsConstructor
@Slf4j
public class OAuth2RedirectController {

    private final OAuth2Properties oAuth2Properties;

    @GetMapping("/oauth2/redirect")
    public RedirectView handleDirectRedirect(HttpServletRequest request) {
        String queryString = request.getQueryString();
        String frontendUrl = oAuth2Properties.getDefaultRedirectUri();
        String redirectTarget = (queryString != null && !queryString.isBlank())
                ? frontendUrl + "?" + queryString
                : frontendUrl;

        log.info("Backend received direct /oauth2/redirect request. Redirecting browser to frontend: {}", redirectTarget);
        return new RedirectView(redirectTarget);
    }
}

