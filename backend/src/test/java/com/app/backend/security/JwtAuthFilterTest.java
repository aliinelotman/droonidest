package com.app.backend.security;

import com.app.backend.TestEntityFactory;
import com.app.backend.model.User;
import com.app.backend.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link JwtAuthFilter}.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    private static final UUID TEST_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGivenNoAuthHeaderWhenFilterThenPassThrough() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void testGivenNonBearerHeaderWhenFilterThenPassThrough() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void testGivenInvalidTokenWhenFilterThenReturn401() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token");
        when(jwtService.parseToken("invalid.token")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void testGivenRefreshTokenWhenFilterThenReturn401() throws Exception {
        Claims claims = mock(Claims.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer refresh.token");
        when(jwtService.parseToken("refresh.token")).thenReturn(claims);
        when(jwtService.isAccessToken(claims)).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void testGivenTokenWithNullRoleWhenFilterThenReturn401() throws Exception {
        Claims claims = mock(Claims.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer access.token");
        when(jwtService.parseToken("access.token")).thenReturn(claims);
        when(jwtService.isAccessToken(claims)).thenReturn(true);
        when(jwtService.extractUserId(claims)).thenReturn(TEST_USER_ID);
        when(jwtService.extractRole(claims)).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token: missing role claim");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void testGivenTokenForDeletedUserWhenFilterThenReturn401() throws Exception {
        Claims claims = mock(Claims.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer access.token");
        when(jwtService.parseToken("access.token")).thenReturn(claims);
        when(jwtService.isAccessToken(claims)).thenReturn(true);
        when(jwtService.extractUserId(claims)).thenReturn(TEST_USER_ID);
        when(jwtService.extractRole(claims)).thenReturn("USER");
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void testGivenAlreadyAuthenticatedRequestWhenFilterThenSkipRepositoryLookupAndPassThrough() throws Exception {
        UsernamePasswordAuthenticationToken existing =
                new UsernamePasswordAuthenticationToken("user", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existing);
        Claims claims = mock(Claims.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer access.token");
        when(jwtService.parseToken("access.token")).thenReturn(claims);
        when(jwtService.isAccessToken(claims)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(userRepository, never()).findById(any());
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void testGivenValidTokenWhenFilterThenPopulateSecurityContextAndPassThrough() throws Exception {
        User user = TestEntityFactory.createUser(TEST_USER_ID, "google-123", "test@example.com", "Test User");
        Claims claims = mock(Claims.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer access.token");
        when(jwtService.parseToken("access.token")).thenReturn(claims);
        when(jwtService.isAccessToken(claims)).thenReturn(true);
        when(jwtService.extractUserId(claims)).thenReturn(TEST_USER_ID);
        when(jwtService.extractRole(claims)).thenReturn("USER");
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isSameAs(user);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_USER");
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }
}
