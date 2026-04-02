package com.app.backend.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Binds Google OAuth 2.0 configuration from application properties.
 */
@Component
@ConfigurationProperties(prefix = "google")
@Validated
@Getter
@Setter
public class GoogleOAuthProperties {

    @NotBlank
    private String clientId;

    @NotBlank
    private String clientSecret;

    @NotBlank
    private String redirectUri;

    @NotBlank
    private String tokenUri;

    @NotBlank
    private String userinfoUri;
}
