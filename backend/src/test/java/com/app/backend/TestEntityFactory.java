package com.app.backend;

import com.app.backend.model.User;

import java.lang.reflect.Field;
import java.util.UUID;

/**
 * Shared test utilities for creating and configuring entity instances.
 */
public final class TestEntityFactory {

    private TestEntityFactory() {
    }

    /**
     * Creates a test User with the given ID set via reflection
     * (since the ID is auto-generated and has no setter).
     */
    public static User createUser(UUID id, String googleId, String email, String displayName) {
        User user = new User(googleId, email, displayName);
        setId(user, id);
        return user;
    }

    /**
     * Sets the {@code id} field on a User entity via reflection.
     */
    public static void setId(User user, UUID id) {
        try {
            Field field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set user ID for test", e);
        }
    }
}
