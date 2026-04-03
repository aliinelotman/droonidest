package com.app.backend.service;

import com.app.backend.config.GoogleOAuthProperties;
import com.app.backend.config.JwtProperties;
import com.app.backend.dto.response.AuthResponse;
import com.app.backend.exception.InvalidTokenException;
import com.app.backend.model.User;
import com.app.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

/**
 * Handles Google OAuth 2.0 authentication and JWT token lifecycle.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final RestClient googleRestClient;
    private final GoogleOAuthProperties googleProperties;
    private final JwtProperties jwtProperties;
    private final JwtService jwtService;
    private final UserService userService;

    /**
     * Exchanges a Google authorization code for tokens, fetches the user profile,
     * finds or creates the user in the database, and returns JWT tokens.
     */
    public AuthResponse authenticateWithGoogle(String authorizationCode) {
        String googleAccessToken = exchangeCodeForAccessToken(authorizationCode);
        Map<String, Object> userInfo = fetchGoogleUserInfo(googleAccessToken);
        User user = resolveUser(userInfo);
        return buildAuthResponse(user);
    }

    /**
     * Validates a refresh token and issues a new access token.
     *
     * @throws InvalidTokenException if the token is invalid or not a refresh token
     */
    public String refreshAccessToken(String refreshToken) {
        var claims = jwtService.parseToken(refreshToken);

        if (claims == null || !jwtService.isRefreshToken(claims)) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        UUID userId = jwtService.extractUserId(claims);
        User user = userService.findById(userId);
        return jwtService.generateAccessToken(user);
    }

    /**
     * Returns the refresh token expiration in seconds, for cookie max-age.
     */
    public long getRefreshTokenMaxAgeSeconds() {
        return jwtProperties.getRefreshTokenExpiration() / 1000;
    }

    private String exchangeCodeForAccessToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("client_id", googleProperties.getClientId());
        formData.add("client_secret", googleProperties.getClientSecret());
        formData.add("redirect_uri", googleProperties.getRedirectUri());
        formData.add("grant_type", "authorization_code");

        Map<String, Object> tokenResponse = googleRestClient.post()
                .uri(googleProperties.getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (tokenResponse == null) {
            throw new InvalidTokenException("Failed to exchange authorization code with Google");
        }

        Object rawToken = tokenResponse.get("access_token");
        if (!(rawToken instanceof String accessToken) || accessToken.isBlank()) {
            throw new InvalidTokenException("Failed to exchange authorization code with Google");
        }

        return accessToken;
    }

    private Map<String, Object> fetchGoogleUserInfo(String accessToken) {
        Map<String, Object> userInfo = googleRestClient.get()
                .uri(googleProperties.getUserinfoUri())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (userInfo == null || !userInfo.containsKey("sub")) {
            throw new InvalidTokenException("Failed to fetch user info from Google");
        }

        return userInfo;
    }

    /**
     * Resolves the user from Google profile data. If a concurrent request creates the same user,
     * catches the unique constraint violation and retries the lookup.
     */
    private User resolveUser(Map<String, Object> userInfo) {
        try {
            return userService.findOrCreateFromGoogle(userInfo);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Concurrent user creation detected for googleId={}, retrying", userInfo.get("sub"));
            try {
                return userService.findOrCreateFromGoogle(userInfo);
            } catch (DataIntegrityViolationException retryEx) {
                log.error("User creation failed after retry — possible duplicate constraint violation");
                throw new InvalidTokenException("Failed to resolve user account", retryEx);
            }
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new AuthResponse(accessToken, userService.toResponse(user), refreshToken);
    }
}
