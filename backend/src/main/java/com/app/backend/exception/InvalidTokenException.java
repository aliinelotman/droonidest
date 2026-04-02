package com.app.backend.exception;

/**
 * Thrown when a JWT token is invalid, expired, or of the wrong type.
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
