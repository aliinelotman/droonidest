package com.app.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for the Google OAuth authorization code exchange.
 */
public record GoogleAuthRequest(
        @NotBlank(message = "Authorization code must not be blank") String code,
        @NotBlank(message = "OAuth state must not be blank") String state) {
}
