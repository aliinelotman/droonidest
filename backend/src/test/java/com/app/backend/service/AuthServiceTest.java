package com.app.backend.service;

import com.app.backend.TestEntityFactory;
import com.app.backend.config.JwtProperties;
import com.app.backend.exception.InvalidTokenException;
import com.app.backend.model.User;
import com.app.backend.security.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthService}.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private UserService userService;

    private AuthService authService;

    private User testUser;
    private static final UUID TEST_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @BeforeEach
    void setUp() {
        authService = new AuthService(null, null, jwtProperties, jwtService, userService);
        testUser = TestEntityFactory.createUser(TEST_USER_ID, "google-123", "test@example.com", "Test User");
    }

    @Test
    void testGivenValidRefreshTokenWhenRefreshThenReturnNewAccessToken() {
        String refreshToken = "valid-refresh-token";
        String expectedAccessToken = "new-access-token";
        Claims mockClaims = mock(Claims.class);

        when(jwtService.parseToken(refreshToken)).thenReturn(mockClaims);
        when(jwtService.isRefreshToken(mockClaims)).thenReturn(true);
        when(jwtService.extractUserId(mockClaims)).thenReturn(TEST_USER_ID);
        when(userService.findById(TEST_USER_ID)).thenReturn(testUser);
        when(jwtService.generateAccessToken(testUser)).thenReturn(expectedAccessToken);

        String result = authService.refreshAccessToken(refreshToken);

        assertThat(result).isEqualTo(expectedAccessToken);
    }

    @Test
    void testGivenInvalidTokenWhenRefreshThenThrowInvalidTokenException() {
        when(jwtService.parseToken("invalid-token")).thenReturn(null);

        assertThatThrownBy(() -> authService.refreshAccessToken("invalid-token"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid or expired refresh token");
    }

    @Test
    void testGivenAccessTokenWhenRefreshThenThrowInvalidTokenException() {
        String accessToken = "access-token";
        Claims mockClaims = mock(Claims.class);

        when(jwtService.parseToken(accessToken)).thenReturn(mockClaims);
        when(jwtService.isRefreshToken(mockClaims)).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshAccessToken(accessToken))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void testGivenConfiguredExpirationWhenGetMaxAgeThenReturnSeconds() {
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604_800_000L);

        long result = authService.getRefreshTokenMaxAgeSeconds();

        assertThat(result).isEqualTo(604_800L);
    }
}
