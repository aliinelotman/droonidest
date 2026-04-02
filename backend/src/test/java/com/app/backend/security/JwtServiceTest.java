package com.app.backend.security;

import com.app.backend.TestEntityFactory;
import com.app.backend.config.JwtProperties;
import com.app.backend.model.User;
import com.app.backend.model.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link JwtService}.
 */
class JwtServiceTest {

    private static final UUID TEST_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha256!!");
        properties.setAccessTokenExpiration(900_000L);
        properties.setRefreshTokenExpiration(604_800_000L);

        jwtService = new JwtService(properties);
        jwtService.initSigningKey();

        testUser = TestEntityFactory.createUser(TEST_USER_ID, "google-123", "test@example.com", "Test User");
    }

    @Test
    void testGivenValidUserWhenGenerateAccessTokenThenReturnValidToken() {
        String token = jwtService.generateAccessToken(testUser);
        Claims claims = jwtService.parseToken(token);

        assertThat(claims).isNotNull();
        assertThat(jwtService.isAccessToken(claims)).isTrue();
        assertThat(jwtService.isRefreshToken(claims)).isFalse();
    }

    @Test
    void testGivenValidUserWhenGenerateRefreshTokenThenReturnValidRefreshToken() {
        String token = jwtService.generateRefreshToken(testUser);
        Claims claims = jwtService.parseToken(token);

        assertThat(claims).isNotNull();
        assertThat(jwtService.isRefreshToken(claims)).isTrue();
        assertThat(jwtService.isAccessToken(claims)).isFalse();
    }

    @Test
    void testGivenValidTokenWhenExtractUserIdThenReturnCorrectId() {
        String token = jwtService.generateAccessToken(testUser);
        Claims claims = jwtService.parseToken(token);

        assertThat(jwtService.extractUserId(claims)).isEqualTo(TEST_USER_ID);
    }

    @Test
    void testGivenValidTokenWhenExtractRoleThenReturnCorrectRole() {
        String token = jwtService.generateAccessToken(testUser);
        Claims claims = jwtService.parseToken(token);

        assertThat(jwtService.extractRole(claims)).isEqualTo(UserRole.USER.name());
    }

    @Test
    void testGivenTamperedTokenWhenParseThenReturnNull() {
        String token = jwtService.generateAccessToken(testUser) + "tampered";

        assertThat(jwtService.parseToken(token)).isNull();
    }

    @Test
    void testGivenExpiredTokenWhenParseThenReturnNull() {
        JwtProperties shortLivedProps = new JwtProperties();
        shortLivedProps.setSecret("test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha256!!");
        shortLivedProps.setAccessTokenExpiration(-1000L);
        shortLivedProps.setRefreshTokenExpiration(604_800_000L);

        JwtService shortLivedService = new JwtService(shortLivedProps);
        shortLivedService.initSigningKey();
        String expiredToken = shortLivedService.generateAccessToken(testUser);

        assertThat(jwtService.parseToken(expiredToken)).isNull();
    }

    @Test
    void testGivenInvalidTokenWhenExtractUserIdThenThrowException() {
        assertThatThrownBy(() -> jwtService.extractUserId("invalid.token.here"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void testGivenGarbageStringWhenValidateThenReturnFalse() {
        assertThat(jwtService.isTokenValid("not-a-jwt")).isFalse();
    }
}
