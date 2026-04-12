package com.app.backend.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Binds Google OAuth 2.0 configuration from application properties.
 */
@ConfigurationProperties(prefix = "google")
@Validated
public record GoogleOAuthProperties(
        @NotBlank String clientId,
        @NotBlank String clientSecret,
        @NotBlank String redirectUri,
        @NotBlank String tokenUri,
        @NotBlank String userinfoUri) {
}
