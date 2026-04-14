package com.app.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Result of a successful authentication.
 * The access token and user profile are sent in the JSON body.
 * The refresh token is excluded from serialization — it is set as an httpOnly cookie by the controller.
 */
public record AuthResponse(
        String accessToken,
        UserResponse user,
        @JsonIgnore String refreshToken) {
}
