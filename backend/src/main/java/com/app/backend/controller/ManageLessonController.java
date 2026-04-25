package com.app.backend.controller;

import com.app.backend.dto.request.CreateLessonRequest;
import com.app.backend.dto.request.UpdateLessonRequest;
import com.app.backend.dto.response.LessonResponse;
import com.app.backend.service.ManageLessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Content management endpoints for lessons.
 * Restricted to users with ADMIN or CONTENT_MANAGER roles.
 */
@RestController
@RequestMapping("/api/v1/manage")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
@Tag(name = "Manage Lessons", description = "Content management endpoints for lessons")
public class ManageLessonController {

    private final ManageLessonService manageLessonService;

    @Operation(
            summary = "Create a new lesson in a module",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Lesson created",
                            content = @Content(schema = @Schema(implementation = LessonResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Module not found", content = @Content)
            }
    )
    @PostMapping("/modules/{moduleId}/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    public LessonResponse createLesson(@PathVariable UUID moduleId, @Valid @RequestBody CreateLessonRequest request) {
        return manageLessonService.create(moduleId, request);
    }

    @Operation(
            summary = "Get lesson by ID (full content)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lesson found",
                            content = @Content(schema = @Schema(implementation = LessonResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Lesson not found", content = @Content)
            }
    )
    @GetMapping("/lessons/{id}")
    public LessonResponse getLesson(@PathVariable UUID id) {
        return manageLessonService.getById(id);
    }

    @Operation(
            summary = "Update lesson fields",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lesson updated",
                            content = @Content(schema = @Schema(implementation = LessonResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Lesson not found", content = @Content)
            }
    )
    @PutMapping("/lessons/{id}")
    public LessonResponse updateLesson(@PathVariable UUID id, @Valid @RequestBody UpdateLessonRequest request) {
        return manageLessonService.update(id, request);
    }

    @Operation(
            summary = "Delete a lesson",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Lesson deleted"),
                    @ApiResponse(responseCode = "404", description = "Lesson not found", content = @Content)
            }
    )
    @DeleteMapping("/lessons/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable UUID id) {
        manageLessonService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
