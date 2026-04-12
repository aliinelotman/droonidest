package com.app.backend.service;

import com.app.backend.TestEntityFactory;
import com.app.backend.config.GoogleOAuthProperties;
import com.app.backend.config.JwtProperties;
import com.app.backend.dto.response.AuthResponse;
import com.app.backend.exception.InvalidTokenException;
import com.app.backend.model.User;
import com.app.backend.security.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthService}.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private RestClient googleRestClient;

    @Mock
    private GoogleOAuthProperties googleProperties;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private UserService userService;

    // Terminal response mocks — the only ones stubbed per-test
    private RestClient.ResponseSpec postResponseSpec;
    private RestClient.ResponseSpec getResponseSpec;

    private AuthService authService;
    private User testUser;
    private static final UUID TEST_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @SuppressWarnings("rawtypes")
    @BeforeEach
    void setUp() {
        authService = new AuthService(googleRestClient, googleProperties, jwtProperties, jwtService, userService);
        testUser = TestEntityFactory.createUser(TEST_USER_ID, "google-123", "test@example.com", "Test User");

        // RETURNS_SELF handles the entire fluent chain (uri, contentType, body, header…)
        // because each of those methods returns the same spec type.
        // We only need to stub retrieve() which breaks the self-returning pattern.
        RestClient.RequestBodyUriSpec postSpec =
                mock(RestClient.RequestBodyUriSpec.class, Answers.RETURNS_SELF);
        postResponseSpec = mock(RestClient.ResponseSpec.class);
        lenient().when(googleRestClient.post()).thenReturn(postSpec);
        lenient().when(postSpec.retrieve()).thenReturn(postResponseSpec);

        RestClient.RequestHeadersUriSpec getSpec =
                mock(RestClient.RequestHeadersUriSpec.class, Answers.RETURNS_SELF);
        getResponseSpec = mock(RestClient.ResponseSpec.class);
        lenient().when(googleRestClient.get()).thenReturn(getSpec);
        lenient().when(getSpec.retrieve()).thenReturn(getResponseSpec);
    }

    // --- buildGoogleAuthorizeUrl ---

    @Test
    void testWhenBuildGoogleAuthorizeUrlThenReturnsUrlWithRequiredParams() {
        when(googleProperties.getClientId()).thenReturn("test-client-id");
        when(googleProperties.getRedirectUri()).thenReturn("http://localhost:4200/auth/callback");

        String url = authService.buildGoogleAuthorizeUrl();

        assertThat(url).startsWith("https://accounts.google.com/o/oauth2/v2/auth");
        assertThat(url).contains("client_id=test-client-id");
        assertThat(url).contains("redirect_uri=http://localhost:4200/auth/callback");
        assertThat(url).contains("response_type=code");
        assertThat(url).contains("scope=openid%20email%20profile");
        assertThat(url).contains("prompt=select_account");
    }

    // --- authenticateWithGoogle ---

    @Test
    @SuppressWarnings("unchecked")
    void testGivenValidGoogleCodeWhenAuthenticateThenReturnAuthResponse() {
        Map<String, Object> tokenResponse = Map.of("access_token", "google-access-token");
        Map<String, Object> userInfo = Map.of(
                "sub", "google-123",
                "email", "test@example.com",
                "name", "Test User",
                "picture", "https://example.com/avatar.jpg",
                "email_verified", true
        );

        when(postResponseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(tokenResponse);
        when(getResponseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(userInfo);
        when(userService.findOrCreateFromGoogle(userInfo)).thenReturn(testUser);
        when(jwtService.generateAccessToken(testUser)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("refresh-token");
        lenient().when(userService.toResponse(testUser)).thenReturn(null);

        AuthResponse result = authService.authenticateWithGoogle("auth-code");

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGivenNullTokenResponseWhenAuthenticateThenThrowInvalidTokenException() {
        when(postResponseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(null);

        assertThatThrownBy(() -> authService.authenticateWithGoogle("bad-code"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Failed to exchange authorization code with Google");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGivenResponseWithoutAccessTokenWhenAuthenticateThenThrowInvalidTokenException() {
        when(postResponseSpec.body(any(ParameterizedTypeReference.class)))
                .thenReturn(Map.of("error", "invalid_grant"));

        assertThatThrownBy(() -> authService.authenticateWithGoogle("expired-code"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Failed to exchange authorization code with Google");
    }

    // --- refreshAuth ---

    @Test
    void testGivenValidRefreshTokenWhenRefreshThenReturnAccessTokenAndUser() {
        Claims mockClaims = mock(Claims.class);

        when(jwtService.parseToken("valid-refresh-token")).thenReturn(mockClaims);
        when(jwtService.isRefreshToken(mockClaims)).thenReturn(true);
        when(jwtService.extractUserId(mockClaims)).thenReturn(TEST_USER_ID);
        when(userService.findById(TEST_USER_ID)).thenReturn(testUser);
        when(jwtService.generateAccessToken(testUser)).thenReturn("new-access-token");
        when(userService.toResponse(testUser)).thenReturn(
                new com.app.backend.dto.response.UserResponse(
                        TEST_USER_ID, "test@example.com", "Test User", null,
                        com.app.backend.model.enums.UserRole.USER));

        AuthResponse result = authService.refreshAuth("valid-refresh-token");

        assertThat(result.getAccessToken()).isEqualTo("new-access-token");
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(result.getRefreshToken()).isNull();
    }

    @Test
    void testGivenInvalidTokenWhenRefreshThenThrowInvalidTokenException() {
        when(jwtService.parseToken("invalid-token")).thenReturn(null);

        assertThatThrownBy(() -> authService.refreshAuth("invalid-token"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid or expired refresh token");
    }

    @Test
    void testGivenAccessTokenWhenRefreshThenThrowInvalidTokenException() {
        Claims mockClaims = mock(Claims.class);

        when(jwtService.parseToken("access-token")).thenReturn(mockClaims);
        when(jwtService.isRefreshToken(mockClaims)).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshAuth("access-token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    // --- getRefreshTokenMaxAgeSeconds ---

    @Test
    void testGivenConfiguredExpirationWhenGetMaxAgeThenReturnSeconds() {
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604_800_000L);

        assertThat(authService.getRefreshTokenMaxAgeSeconds()).isEqualTo(604_800L);
    }
}
