package com.app.backend.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Binds JWT configuration from application properties.
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Validated
@Getter
@Setter
public class JwtProperties {

    @NotBlank
    @Size(min = 32, message = "JWT secret must be at least 32 characters (256 bits)")
    private String secret;

    @Positive
    private long accessTokenExpiration;

    @Positive
    private long refreshTokenExpiration;
}
