package com.bankwise.security.oauth2.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "application.security.oauth2")
public class OAuth2Properties {

    private List<String> authorizedRedirectUris = new ArrayList<>(List.of(
            "http://localhost:3000/oauth2/redirect",
            "http://localhost:5173/oauth2/redirect"
    ));

    private String defaultRedirectUri = "http://localhost:3000/oauth2/redirect";
}

