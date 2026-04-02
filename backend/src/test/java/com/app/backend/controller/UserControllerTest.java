package com.app.backend.controller;

import com.app.backend.TestEntityFactory;
import com.app.backend.config.CorsProperties;
import com.app.backend.config.SecurityConfig;
import com.app.backend.dto.response.UserResponse;
import com.app.backend.model.User;
import com.app.backend.model.enums.UserRole;
import com.app.backend.security.JwtAuthFilter;
import com.app.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice tests for {@link UserController}.
 */
@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, CorsProperties.class})
@TestPropertySource(properties = "cors.allowed-origins=http://localhost:4200")
class UserControllerTest {

    private static final UUID TEST_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            jakarta.servlet.ServletRequest request = invocation.getArgument(0);
            jakarta.servlet.ServletResponse response = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());

        testUser = TestEntityFactory.createUser(
                TEST_USER_ID, "google-123", "test@example.com", "Test User");
        testUser.setAvatarUrl("https://example.com/avatar.jpg");
    }

    @Test
    void testGivenAuthenticatedUserWhenGetMeThenReturnUserProfile() throws Exception {
        UserResponse userResponse = new UserResponse(
                TEST_USER_ID, "test@example.com", "Test User",
                "https://example.com/avatar.jpg", UserRole.USER);
        when(userService.toResponse(any(User.class))).thenReturn(userResponse);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                testUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        mockMvc.perform(get("/api/v1/users/me")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_USER_ID.toString()))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.displayName").value("Test User"))
                .andExpect(jsonPath("$.avatarUrl").value("https://example.com/avatar.jpg"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void testGivenNoAuthenticationWhenGetMeThenReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isForbidden());
    }
}
