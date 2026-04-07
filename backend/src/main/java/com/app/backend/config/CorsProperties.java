package com.app.backend.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Binds CORS configuration from application properties.
 */
@Component
@ConfigurationProperties(prefix = "cors")
@Validated
@Getter
@Setter
public class CorsProperties {

    @NotBlank
    private String allowedOrigins;
}
