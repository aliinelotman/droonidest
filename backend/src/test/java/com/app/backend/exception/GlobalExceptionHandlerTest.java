package com.app.backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link GlobalExceptionHandler}.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testGivenResourceNotFoundWhenHandleThenReturn404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User", "123");

        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().get("message")).asString().contains("123");
    }

    @Test
    void testGivenInvalidTokenWhenHandleThenReturn401() {
        InvalidTokenException ex = new InvalidTokenException("Token expired");

        ResponseEntity<Map<String, Object>> response = handler.handleInvalidToken(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("UNAUTHORIZED");
        assertThat(response.getBody().get("message")).isEqualTo("Token expired");
    }

    @Test
    void testGivenAccessDeniedWhenHandleThenReturn403() {
        org.springframework.security.access.AccessDeniedException ex =
                new org.springframework.security.access.AccessDeniedException("Forbidden");

        ResponseEntity<Map<String, Object>> response = handler.handleAccessDenied(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("FORBIDDEN");
    }

    @Test
    void testGivenUnhandledExceptionWhenHandleThenReturn500WithGenericMessage() {
        Exception ex = new RuntimeException("Something broke");

        ResponseEntity<Map<String, Object>> response = handler.handleGeneric(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().get("message")).isEqualTo("An unexpected error occurred");
    }

    @Test
    void testGivenTypeMismatchWhenHandleThenReturn400() {
        MethodArgumentTypeMismatchException ex =
                new MethodArgumentTypeMismatchException("abc", Integer.class, "id", null, null);

        ResponseEntity<Map<String, Object>> response = handler.handleTypeMismatch(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("BAD_REQUEST");
    }

    @Test
    void testGivenAnyExceptionWhenHandleThenResponseContainsTimestamp() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Test");

        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);

        assertThat(response.getBody()).containsKey("timestamp");
    }
}
