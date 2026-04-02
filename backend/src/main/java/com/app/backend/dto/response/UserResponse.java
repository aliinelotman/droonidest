package com.app.backend.dto.response;

import com.app.backend.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * User profile data returned in API responses.
 */
@Getter
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String email;
    private String displayName;
    private String avatarUrl;
    private UserRole role;
}
