package com.app.backend;

import com.app.backend.model.Lesson;
import com.app.backend.model.Module;
import com.app.backend.model.User;

import java.lang.reflect.Field;
import java.util.UUID;

/**
 * Shared test utilities for creating and configuring entity instances.
 */
public final class TestEntityFactory {

    private TestEntityFactory() {
    }

    public static User createUser(UUID id, String googleId, String email, String displayName) {
        User user = new User(googleId, email, displayName);
        setId(user, "id", User.class, id);
        return user;
    }

    public static Module createModule(UUID id, String title, int sortOrder) {
        Module module = new Module(title, sortOrder);
        setId(module, "id", Module.class, id);
        return module;
    }

    public static Lesson createLesson(UUID id, String title) {
        Lesson lesson = new Lesson(title);
        setId(lesson, "id", Lesson.class, id);
        return lesson;
    }

    public static void setId(User user, UUID id) {
        setId(user, "id", User.class, id);
    }

    private static void setId(Object entity, String fieldName, Class<?> clazz, UUID id) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(entity, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set " + fieldName + " for test", e);
        }
    }
}
