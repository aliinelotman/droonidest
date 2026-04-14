package com.app.backend.controller;

import com.app.backend.dto.request.GoogleAuthRequest;
import com.app.backend.dto.response.AuthResponse;
import com.app.backend.exception.InvalidTokenException;
import com.app.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * Handles authentication endpoints: Google OAuth login, token refresh, and logout.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Google OAuth login, token refresh, and logout")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final AuthService authService;

    @Value("${auth.cookie.secure:true}")
    private boolean secureCookie;

    /**
     * Returns the Google OAuth 2.0 authorization URL the frontend should open to
     * initiate login. Keeps the Google client ID on the backend.
     */
    @Operation(
            summary = "Get Google OAuth authorize URL",
            description = "Returns the Google OAuth 2.0 authorization URL the frontend should open " +
                    "to initiate login.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authorize URL returned")
            }
    )
    @SecurityRequirements
    @GetMapping("/google/authorize-url")
    public ResponseEntity<Map<String, String>> getGoogleAuthorizeUrl() {
        return ResponseEntity.ok(Map.of("url", authService.buildGoogleAuthorizeUrl()));
    }

    /**
     * Exchanges a Google authorization code for JWT tokens.
     * Access token and user profile are returned in the response body.
     * Refresh token is set as an httpOnly cookie.
     */
    @Operation(
            summary = "Google OAuth login",
            description = "Exchanges a Google authorization code for JWT tokens. " +
                    "Returns access token in body and sets refresh token as an httpOnly cookie.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authentication successful",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Invalid or expired Google code", content = @Content)
            }
    )
    @SecurityRequirements
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> authenticateWithGoogle(
            @Valid @RequestBody GoogleAuthRequest request,
            HttpServletResponse response) {

        AuthResponse authResponse = authService.authenticateWithGoogle(request.code(), request.state());
        setRefreshTokenCookie(response, authResponse.refreshToken());
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Refreshes the access token using the refresh token from the httpOnly cookie.
     */
    @Operation(
            summary = "Refresh access token",
            description = "Issues a new access token using the refresh token from the httpOnly cookie. " +
                    "Returns 204 when no cookie is present (user not logged in) and 401 when the token is invalid or expired.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "New access token issued"),
                    @ApiResponse(responseCode = "204", description = "No refresh token cookie present", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token", content = @Content)
            }
    )
    @SecurityRequirements
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        Optional<String> refreshToken = extractRefreshTokenFromCookies(request);
        if (refreshToken.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        AuthResponse authResponse = authService.refreshAuth(refreshToken.get());
        setRefreshTokenCookie(response, authResponse.refreshToken());
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Clears the refresh token cookie, effectively logging the user out.
     */
    @Operation(
            summary = "Logout",
            description = "Clears the refresh token cookie, ending the user session.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Logged out successfully")
            }
    )
    @SecurityRequirements
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        clearRefreshTokenCookie(response);
        return ResponseEntity.noContent().build();
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(authService.getRefreshTokenMaxAgeSeconds())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private Optional<String> extractRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> REFRESH_TOKEN_COOKIE.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
