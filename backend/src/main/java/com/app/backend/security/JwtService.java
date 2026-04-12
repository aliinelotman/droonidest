package com.app.backend.security;

import com.app.backend.config.JwtProperties;
import com.app.backend.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Generates and validates JWT access and refresh tokens.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_TOKEN_TYPE = "type";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";
    private static final String TOKEN_TYPE_STATE = "state";
    private static final long STATE_TOKEN_EXPIRATION_MS = 10 * 60 * 1_000L;

    private final JwtProperties jwtProperties;
    private SecretKey signingKey;

    @PostConstruct
    void initSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a short-lived access token containing user claims.
     */
    public String generateAccessToken(User user) {
        return buildToken(user, TOKEN_TYPE_ACCESS, jwtProperties.getAccessTokenExpiration());
    }

    /**
     * Generates a long-lived refresh token.
     */
    public String generateRefreshToken(User user) {
        return buildToken(user, TOKEN_TYPE_REFRESH, jwtProperties.getRefreshTokenExpiration());
    }

    /**
     * Generates a short-lived state token for OAuth CSRF protection.
     */
    public String generateStateToken() {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + STATE_TOKEN_EXPIRATION_MS);
        return Jwts.builder()
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_STATE)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Returns true if the claims represent a state token.
     */
    public boolean isStateToken(Claims claims) {
        return TOKEN_TYPE_STATE.equals(claims.get(CLAIM_TOKEN_TYPE, String.class));
    }

    /**
     * Parses and validates the token in a single operation. Returns null if invalid.
     * Callers should use this instead of calling multiple methods that each re-parse.
     */
    public Claims parseToken(String token) {
        try {
            return extractAllClaims(token);
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Extracts the user ID (subject) from pre-parsed claims.
     */
    public UUID extractUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    /**
     * Extracts the role claim from pre-parsed claims.
     */
    public String extractRole(Claims claims) {
        return claims.get(CLAIM_ROLE, String.class);
    }

    /**
     * Returns true if the claims represent an access token.
     */
    public boolean isAccessToken(Claims claims) {
        return TOKEN_TYPE_ACCESS.equals(claims.get(CLAIM_TOKEN_TYPE, String.class));
    }

    /**
     * Returns true if the claims represent a refresh token.
     */
    public boolean isRefreshToken(Claims claims) {
        return TOKEN_TYPE_REFRESH.equals(claims.get(CLAIM_TOKEN_TYPE, String.class));
    }

    private String buildToken(User user, String tokenType, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
