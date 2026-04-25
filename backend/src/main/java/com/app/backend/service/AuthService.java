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
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.UUID;

/**
 * Handles Google OAuth 2.0 authentication and JWT token lifecycle.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String GOOGLE_AUTHORIZE_URI = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_OAUTH_SCOPES = "openid email profile";

    private final RestClient googleRestClient;
    private final GoogleOAuthProperties googleProperties;
    private final JwtProperties jwtProperties;
    private final JwtService jwtService;
    private final UserService userService;

    /**
     * Builds the Google OAuth 2.0 authorization URL the frontend should redirect to
     * when initiating login. Includes a signed state token for CSRF protection.
     */
    public String buildGoogleAuthorizeUrl() {
        String state = jwtService.generateStateToken();
        log.debug("Generated OAuth state token for Google authorize URL");
        return UriComponentsBuilder.fromUriString(GOOGLE_AUTHORIZE_URI)
                .queryParam("client_id", googleProperties.clientId())
                .queryParam("redirect_uri", googleProperties.redirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", GOOGLE_OAUTH_SCOPES)
                .queryParam("access_type", "online")
                .queryParam("prompt", "select_account")
                .queryParam("state", state)
                .build()
                .encode()
                .toUriString();
    }

    /**
     * Validates the OAuth state token, exchanges the authorization code for tokens,
     * fetches the user profile, finds or creates the user, and returns JWT tokens.
     *
     * @throws InvalidTokenException if the state token is invalid or expired
     */
    public AuthResponse authenticateWithGoogle(String authorizationCode, String state) {
        log.info("Authenticating user with Google OAuth");
        validateOAuthState(state);
        String googleAccessToken = exchangeCodeForAccessToken(authorizationCode);
        Map<String, Object> userInfo = fetchGoogleUserInfo(googleAccessToken);
        User user = resolveUser(userInfo);
        return buildAuthResponse(user);
    }

    /**
     * Validates a refresh token, issues a new access token and rotated refresh token,
     * and returns the user profile. The user profile is included so the frontend can
     * restore session state on page reload without a separate profile fetch.
     *
     * @throws InvalidTokenException if the token is invalid or not a refresh token
     */
    public AuthResponse refreshAuth(String refreshToken) {
        log.debug("Refreshing authentication session");
        var claims = jwtService.parseToken(refreshToken);

        if (claims == null || !jwtService.isRefreshToken(claims)) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        UUID userId = jwtService.extractUserId(claims);
        User user = userService.findById(userId);
        String accessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        log.info("Refreshed authentication userId={}", userId);
        return new AuthResponse(accessToken, userService.toResponse(user), newRefreshToken);
    }

    /**
     * Returns the refresh token expiration in seconds, for cookie max-age.
     */
    public long getRefreshTokenMaxAgeSeconds() {
        return jwtProperties.getRefreshTokenExpiration() / 1000;
    }

    private void validateOAuthState(String state) {
        log.debug("Validating OAuth state token");
        var claims = jwtService.parseToken(state);
        if (claims == null || !jwtService.isStateToken(claims)) {
            throw new InvalidTokenException("Invalid or expired OAuth state");
        }
    }

    private String exchangeCodeForAccessToken(String code) {
        log.debug("Exchanging Google authorization code for access token");
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("client_id", googleProperties.clientId());
        formData.add("client_secret", googleProperties.clientSecret());
        formData.add("redirect_uri", googleProperties.redirectUri());
        formData.add("grant_type", "authorization_code");

        Map<String, Object> tokenResponse = googleRestClient.post()
                .uri(googleProperties.tokenUri())
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

        log.debug("Received Google access token response");
        return accessToken;
    }

    private Map<String, Object> fetchGoogleUserInfo(String accessToken) {
        log.debug("Fetching Google user info");
        Map<String, Object> userInfo = googleRestClient.get()
                .uri(googleProperties.userinfoUri())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (userInfo == null || !userInfo.containsKey("sub")) {
            throw new InvalidTokenException("Failed to fetch user info from Google");
        }

        log.debug("Fetched Google user info");
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
            log.warn("Concurrent user creation detected during Google authentication, retrying");
            try {
                return userService.findOrCreateFromGoogle(userInfo);
            } catch (DataIntegrityViolationException retryEx) {
                log.error("User creation failed after retry; possible duplicate constraint violation");
                throw new InvalidTokenException("Failed to resolve user account", retryEx);
            }
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        log.info("User authenticated via Google userId={} role={}", user.getId(), user.getRole());
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new AuthResponse(accessToken, userService.toResponse(user), refreshToken);
    }
}
