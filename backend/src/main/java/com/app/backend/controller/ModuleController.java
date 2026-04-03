package com.app.backend.controller;

import com.app.backend.dto.response.LessonResponse;
import com.app.backend.dto.response.ModuleResponse;
import com.app.backend.service.LessonService;
import com.app.backend.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;
    private final LessonService lessonService;

    @GetMapping
    public List<ModuleResponse> getAllModules() {
        return moduleService.getAll();
    }

    @GetMapping("/{id}")
    public ModuleResponse getModule(@PathVariable UUID id) {
        return moduleService.getById(id);
    }

    @GetMapping("/{moduleId}/lessons")
    public List<LessonResponse> getLessonsByModule(@PathVariable UUID moduleId) {
        return lessonService.getByModuleId(moduleId);
    }
}
