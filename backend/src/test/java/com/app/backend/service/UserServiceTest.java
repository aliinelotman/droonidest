package com.app.backend.service;

import com.app.backend.TestEntityFactory;
import com.app.backend.dto.response.UserResponse;
import com.app.backend.exception.InvalidTokenException;
import com.app.backend.exception.ResourceNotFoundException;
import com.app.backend.model.User;
import com.app.backend.model.enums.UserRole;
import com.app.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserService}.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testGivenExistingGoogleIdWhenFindOrCreateThenUpdateAndReturnExisting() {
        User existingUser = TestEntityFactory.createUser(
                UUID.randomUUID(), "google-123", "test@example.com", "Test User");
        existingUser.setAvatarUrl("https://example.com/avatar.jpg");
        Map<String, Object> googleInfo = createGoogleUserInfo();
        when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.of(existingUser));

        User result = userService.findOrCreateFromGoogle(googleInfo);

        assertThat(result).isSameAs(existingUser);
        assertThat(result.getDisplayName()).isEqualTo("Test User");
        verify(userRepository, never()).save(any());
    }

    @Test
    void testGivenNewGoogleIdWhenFindOrCreateThenCreateNewUser() {
        Map<String, Object> googleInfo = createGoogleUserInfo();
        when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            TestEntityFactory.setId(saved, UUID.randomUUID());
            return saved;
        });

        User result = userService.findOrCreateFromGoogle(googleInfo);

        assertThat(result.getGoogleId()).isEqualTo("google-123");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testGivenExistingIdWhenFindByIdThenReturnUser() {
        UUID userId = UUID.randomUUID();
        User user = TestEntityFactory.createUser(userId, "google-123", "test@example.com", "Test User");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.findById(userId);

        assertThat(result).isSameAs(user);
    }

    @Test
    void testGivenNonExistingIdWhenFindByIdThenThrowResourceNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(userId.toString());
    }

    @Test
    void testGivenUserWhenToResponseThenMapAllFieldsCorrectly() {
        UUID userId = UUID.randomUUID();
        User user = TestEntityFactory.createUser(userId, "google-123", "test@example.com", "Test User");

        UserResponse response = userService.toResponse(user);

        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getDisplayName()).isEqualTo("Test User");
        assertThat(response.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void testGivenMissingSubWhenFindOrCreateThenThrowInvalidTokenException() {
        Map<String, Object> googleInfo = Map.of(
                "email", "test@example.com",
                "name", "Test User"
        );

        assertThatThrownBy(() -> userService.findOrCreateFromGoogle(googleInfo))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Google user ID");
    }

    @Test
    void testGivenMissingEmailWhenFindOrCreateThenThrowInvalidTokenException() {
        Map<String, Object> googleInfo = Map.of(
                "sub", "google-123",
                "name", "Test User"
        );

        assertThatThrownBy(() -> userService.findOrCreateFromGoogle(googleInfo))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("email");
    }

    private Map<String, Object> createGoogleUserInfo() {
        return Map.of(
                "sub", "google-123",
                "email", "test@example.com",
                "name", "Test User",
                "picture", "https://example.com/avatar.jpg",
                "email_verified", true
        );
    }
}
