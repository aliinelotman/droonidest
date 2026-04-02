package com.app.backend.controller;

import com.app.backend.dto.request.GoogleAuthRequest;
import com.app.backend.dto.response.AuthResponse;
import com.app.backend.exception.InvalidTokenException;
import com.app.backend.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

/**
 * Handles authentication endpoints: Google OAuth login, token refresh, and logout.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final String COOKIE_ATTRIBUTES = "Path=/api/v1/auth; HttpOnly; Secure; SameSite=Strict";

    private final AuthService authService;

    /**
     * Exchanges a Google authorization code for JWT tokens.
     * Access token and user profile are returned in the response body.
     * Refresh token is set as an httpOnly cookie.
     */
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> authenticateWithGoogle(
            @Valid @RequestBody GoogleAuthRequest request,
            HttpServletResponse response) {

        AuthResponse authResponse = authService.authenticateWithGoogle(request.getCode());
        setRefreshTokenCookie(response, authResponse.getRefreshToken());
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Refreshes the access token using the refresh token from the httpOnly cookie.
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(HttpServletRequest request) {
        String refreshToken = extractRefreshTokenFromCookies(request);

        if (refreshToken == null) {
            throw new InvalidTokenException("Refresh token cookie not found");
        }

        String newAccessToken = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    /**
     * Clears the refresh token cookie, effectively logging the user out.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        clearRefreshTokenCookie(response);
        return ResponseEntity.noContent().build();
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        long maxAge = authService.getRefreshTokenMaxAgeSeconds();
        String cookieValue = String.format(
                "%s=%s; %s; Max-Age=%d",
                REFRESH_TOKEN_COOKIE, refreshToken, COOKIE_ATTRIBUTES, maxAge);
        response.addHeader("Set-Cookie", cookieValue);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        String cookieValue = String.format(
                "%s=; %s; Max-Age=0",
                REFRESH_TOKEN_COOKIE, COOKIE_ATTRIBUTES);
        response.addHeader("Set-Cookie", cookieValue);
    }

    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> REFRESH_TOKEN_COOKIE.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
