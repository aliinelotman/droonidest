package com.app.backend.controller;

import com.app.backend.dto.request.CreateModuleRequest;
import com.app.backend.dto.request.UpdateModuleRequest;
import com.app.backend.dto.response.ModuleDetailResponse;
import com.app.backend.dto.response.ModuleResponse;
import com.app.backend.service.ManageModuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Content management endpoints for modules.
 * Restricted to users with ADMIN or CONTENT_MANAGER roles.
 */
@RestController
@RequestMapping("/api/v1/manage/modules")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
@Tag(name = "Manage Modules", description = "Content management endpoints for modules")
public class ManageModuleController {

    private final ManageModuleService manageModuleService;

    /**
     * Creates a new module with auto-assigned sort order.
     */
    @Operation(
            summary = "Create a new module",
            responses = @ApiResponse(responseCode = "201", description = "Module created",
                    content = @Content(schema = @Schema(implementation = ModuleResponse.class)))
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ModuleResponse createModule(@Valid @RequestBody CreateModuleRequest request) {
        log.info("Creating module title={}", request.getTitle());
        ModuleResponse module = manageModuleService.create(request);
        log.info("Created module id={} title={}", module.getId(), module.getTitle());
        return module;
    }

    /**
     * Returns all modules regardless of status, ordered by sort order.
     */
    @Operation(
            summary = "List all modules (all statuses)",
            responses = @ApiResponse(responseCode = "200", description = "Module list returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ModuleResponse.class))))
    )
    @GetMapping
    public List<ModuleResponse> getAllModules() {
        log.debug("Listing all managed modules");
        List<ModuleResponse> modules = manageModuleService.getAll();
        log.debug("Returning {} managed modules", modules.size());
        return modules;
    }

    /**
     * Returns a single module with its nested lesson summaries.
     */
    @Operation(
            summary = "Get module by ID with lesson summaries",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Module found",
                            content = @Content(schema = @Schema(implementation = ModuleDetailResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Module not found", content = @Content)
            }
    )
    @GetMapping("/{id}")
    public ModuleDetailResponse getModule(@PathVariable UUID id) {
        log.debug("Fetching managed module id={}", id);
        ModuleDetailResponse module = manageModuleService.getById(id);
        log.debug("Returning managed module id={}", id);
        return module;
    }

    /**
     * Updates all module fields including status.
     */
    @Operation(
            summary = "Update module fields including status",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Module updated",
                            content = @Content(schema = @Schema(implementation = ModuleResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Module not found", content = @Content)
            }
    )
    @PutMapping("/{id}")
    public ModuleResponse updateModule(@PathVariable UUID id, @Valid @RequestBody UpdateModuleRequest request) {
        log.info("Updating module id={} title={} status={}", id, request.getTitle(), request.getStatus());
        ModuleResponse module = manageModuleService.update(id, request);
        log.info("Updated module id={} status={}", module.getId(), module.getStatus());
        return module;
    }

    /**
     * Deletes a module and its module–lesson associations.
     */
    @Operation(
            summary = "Delete a module",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Module deleted"),
                    @ApiResponse(responseCode = "404", description = "Module not found", content = @Content)
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable UUID id) {
        log.info("Deleting module id={}", id);
        manageModuleService.delete(id);
        log.info("Deleted module id={}", id);
        return ResponseEntity.noContent().build();
    }
}
