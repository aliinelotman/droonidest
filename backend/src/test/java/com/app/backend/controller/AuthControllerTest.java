package com.app.backend.controller;

import com.app.backend.config.CorsProperties;
import com.app.backend.config.SecurityConfig;
import com.app.backend.dto.response.AuthResponse;
import com.app.backend.dto.response.UserResponse;
import com.app.backend.exception.InvalidTokenException;
import com.app.backend.model.enums.UserRole;
import com.app.backend.security.JwtAuthFilter;
import com.app.backend.service.AuthService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice tests for {@link AuthController}.
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, CorsProperties.class})
@TestPropertySource(properties = "cors.allowed-origins=http://localhost:4200")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            jakarta.servlet.ServletRequest request = invocation.getArgument(0);
            jakarta.servlet.ServletResponse response = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    @Test
    void testGivenValidCodeWhenGoogleAuthThenReturnTokenAndSetCookie() throws Exception {
        UserResponse userResponse = new UserResponse(
                UUID.randomUUID(), "test@example.com", "Test User", null, UserRole.USER);
        AuthResponse authResponse = new AuthResponse("access-token-123", userResponse, "refresh-token-456");

        when(authService.authenticateWithGoogle("valid-code")).thenReturn(authResponse);
        when(authService.getRefreshTokenMaxAgeSeconds()).thenReturn(604_800L);

        mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"valid-code\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token-123"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    void testGivenBlankCodeWhenGoogleAuthThenReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGivenMissingBodyWhenGoogleAuthThenReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGivenValidCookieWhenRefreshThenReturnNewAccessToken() throws Exception {
        when(authService.refreshAccessToken("valid-refresh-token")).thenReturn("new-access-token");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refresh_token", "valid-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));
    }

    @Test
    void testGivenNoCookieWhenRefreshThenReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGivenInvalidTokenWhenRefreshThenReturnUnauthorized() throws Exception {
        when(authService.refreshAccessToken("bad-token"))
                .thenThrow(new InvalidTokenException("Invalid or expired refresh token"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refresh_token", "bad-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void testGivenAuthenticatedUserWhenLogoutThenClearCookieAndReturnNoContent() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie",
                        org.hamcrest.Matchers.containsString("Max-Age=0")));
    }
}
