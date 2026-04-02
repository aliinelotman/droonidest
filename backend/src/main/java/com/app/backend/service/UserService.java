package com.app.backend.service;

import com.app.backend.dto.response.UserResponse;
import com.app.backend.exception.ResourceNotFoundException;
import com.app.backend.model.User;
import com.app.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Manages user lookup and creation operations.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * Finds an existing user by Google ID, or creates a new one from Google profile data.
     * Called during OAuth login — the caller handles the race-condition retry.
     */
    @Transactional
    public User findOrCreateFromGoogle(Map<String, Object> googleUserInfo) {
        String googleId = (String) googleUserInfo.get("sub");
        String email = (String) googleUserInfo.get("email");
        String displayName = (String) googleUserInfo.get("name");
        String avatarUrl = (String) googleUserInfo.get("picture");
        boolean emailVerified = Boolean.TRUE.equals(googleUserInfo.get("email_verified"));

        return userRepository.findByGoogleId(googleId)
                .map(existingUser -> updateExistingUser(existingUser, displayName, avatarUrl, emailVerified))
                .orElseGet(() -> createNewUser(googleId, email, displayName, avatarUrl, emailVerified));
    }

    /**
     * Loads a user by ID. Soft-deleted users are excluded by {@code @SQLRestriction}.
     */
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    /**
     * Maps a User entity to a UserResponse DTO.
     */
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getRole()
        );
    }

    private User updateExistingUser(User user, String displayName, String avatarUrl, boolean emailVerified) {
        user.setDisplayName(displayName);
        user.setAvatarUrl(avatarUrl);
        user.setEmailVerified(emailVerified);
        return user;
    }

    private User createNewUser(String googleId, String email, String displayName,
                               String avatarUrl, boolean emailVerified) {
        User user = new User(googleId, email, displayName);
        user.setAvatarUrl(avatarUrl);
        user.setEmailVerified(emailVerified);
        return userRepository.save(user);
    }
}
