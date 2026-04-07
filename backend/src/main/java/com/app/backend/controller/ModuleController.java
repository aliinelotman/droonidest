package com.app.backend.controller;

import com.app.backend.dto.response.LessonResponse;
import com.app.backend.dto.response.ModuleResponse;
import com.app.backend.service.LessonService;
import com.app.backend.service.ModuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
@Tag(name = "Modules", description = "Course module and lesson browsing (public)")
public class ModuleController {

    private final ModuleService moduleService;
    private final LessonService lessonService;

    @Operation(
            summary = "List all modules",
            responses = @ApiResponse(responseCode = "200", description = "Module list returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ModuleResponse.class))))
    )
    @SecurityRequirements
    @GetMapping
    public List<ModuleResponse> getAllModules() {
        return moduleService.getAll();
    }

    @Operation(
            summary = "Get module by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Module found",
                            content = @Content(schema = @Schema(implementation = ModuleResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Module not found", content = @Content)
            }
    )
    @SecurityRequirements
    @GetMapping("/{id}")
    public ModuleResponse getModule(@PathVariable UUID id) {
        return moduleService.getById(id);
    }

    @Operation(
            summary = "List lessons in a module",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lesson list returned",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = LessonResponse.class)))),
                    @ApiResponse(responseCode = "404", description = "Module not found", content = @Content)
            }
    )
    @SecurityRequirements
    @GetMapping("/{moduleId}/lessons")
    public List<LessonResponse> getLessonsByModule(@PathVariable UUID moduleId) {
        return lessonService.getByModuleId(moduleId);
    }
}
