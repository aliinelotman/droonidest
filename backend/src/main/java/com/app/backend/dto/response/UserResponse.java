package com.app.backend.dto.response;

import com.app.backend.model.enums.UserRole;

import java.util.UUID;

/**
 * User profile data returned in API responses.
 */
public record UserResponse(UUID id, String email, String displayName, String avatarUrl, UserRole role) {
}
