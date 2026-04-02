package com.app.backend.controller;

import com.app.backend.dto.response.UserResponse;
import com.app.backend.model.User;
import com.app.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides endpoints for the authenticated user's profile.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Authenticated user profile endpoints")
public class UserController {

    private final UserService userService;

    /**
     * Returns the profile of the currently authenticated user.
     */
    @Operation(
            summary = "Get current user",
            description = "Returns the profile of the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User profile returned",
                            content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Not authenticated", content = @Content)
            }
    )
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.toResponse(currentUser));
    }
}
