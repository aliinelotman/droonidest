package com.app.backend.service;

import com.app.backend.dto.response.UserResponse;
import com.app.backend.exception.InvalidTokenException;
import com.app.backend.exception.ResourceNotFoundException;
import com.app.backend.model.User;
import com.app.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Manages user lookup and creation operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private record GoogleProfile(String googleId, String email, String displayName,
                                 String avatarUrl, boolean emailVerified) {}

    private final UserRepository userRepository;

    /**
     * Finds an existing user by Google ID, or creates a new one from Google profile data.
     * Called during OAuth login — the caller handles the race-condition retry.
     */
    @Transactional
    public User findOrCreateFromGoogle(Map<String, Object> googleUserInfo) {
        if (!(googleUserInfo.get("sub") instanceof String googleId)) {
            throw new InvalidTokenException("Missing or invalid Google user ID");
        }
        if (!(googleUserInfo.get("email") instanceof String email)) {
            throw new InvalidTokenException("Missing or invalid email in Google profile");
        }
        String displayName = googleUserInfo.get("name") instanceof String s ? s : null;
        String avatarUrl = googleUserInfo.get("picture") instanceof String s ? s : null;
        boolean emailVerified = Boolean.TRUE.equals(googleUserInfo.get("email_verified"));

        GoogleProfile profile = new GoogleProfile(googleId, email, displayName, avatarUrl, emailVerified);

        log.info("Processing Google user profile: googleId={}, email={}", googleId, email);
        return userRepository.findByGoogleId(googleId)
                .map(existingUser -> {
                    log.info("Updating existing user id={} for googleId={}", existingUser.getId(), googleId);
                    return updateExistingUser(existingUser, profile);
                })
                .orElseGet(() -> createNewUser(profile));
    }

    /**
     * Loads a user by ID. Soft-deleted users are excluded by {@code @SQLRestriction}.
     */
    public User findById(UUID id) {
        log.debug("Looking up user id={}", id);
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

    private User updateExistingUser(User user, GoogleProfile profile) {
        user.setDisplayName(profile.displayName());
        user.setAvatarUrl(profile.avatarUrl());
        user.setEmailVerified(profile.emailVerified());
        return user;
    }

    private User createNewUser(GoogleProfile profile) {
        log.info("Creating new user for googleId={} email={}", profile.googleId(), profile.email());
        User user = new User(profile.googleId(), profile.email(), profile.displayName());
        user.setAvatarUrl(profile.avatarUrl());
        user.setEmailVerified(profile.emailVerified());
        User savedUser = userRepository.save(user);
        log.info("Created new user id={} for googleId={}", savedUser.getId(), profile.googleId());
        return savedUser;
    }
}
