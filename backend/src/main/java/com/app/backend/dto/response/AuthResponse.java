package com.app.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Result of a successful authentication.
 * The access token and user profile are sent in the JSON body.
 * The refresh token is excluded from serialization — it is set as an httpOnly cookie by the controller.
 */
@Getter
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private UserResponse user;

    @JsonIgnore
    private String refreshToken;
}
