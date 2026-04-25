package com.app.backend.service;

import com.app.backend.dto.request.CreateModuleRequest;
import com.app.backend.dto.request.UpdateModuleRequest;
import com.app.backend.dto.response.ModuleDetailResponse;
import com.app.backend.dto.response.ModuleDetailResponse.LessonSummary;
import com.app.backend.dto.response.ModuleResponse;
import com.app.backend.exception.ResourceNotFoundException;
import com.app.backend.model.Module;
import com.app.backend.repository.ModuleLessonRepository;
import com.app.backend.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Business logic for content-management module operations (CRUD).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ManageModuleService {

    private final ModuleRepository moduleRepository;
    private final ModuleLessonRepository moduleLessonRepository;

    /**
     * Creates a new module with sort order appended after existing modules.
     */
    @Transactional
    public ModuleResponse create(CreateModuleRequest request) {
        log.info("Creating module title={}", request.getTitle());
        int nextSortOrder = (int) moduleRepository.count();
        Module module = new Module(request.getTitle(), nextSortOrder);
        module.setDescription(request.getDescription());
        module.setThumbnailUrl(request.getThumbnailUrl());
        module.setIfFreePreview(request.isIfFreePreview());
        ModuleResponse response = ModuleResponse.from(moduleRepository.save(module));
        log.info("Created module id={} sortOrder={}", response.getId(), response.getSortOrder());
        return response;
    }

    /**
     * Returns all modules regardless of status, ordered by sort order.
     */
    public List<ModuleResponse> getAll() {
        List<ModuleResponse> modules = moduleRepository.findAllByOrderBySortOrder()
                .stream()
                .map(ModuleResponse::from)
                .toList();
        log.debug("Loaded {} managed modules", modules.size());
        return modules;
    }

    /**
     * Returns a module with its nested lesson summaries.
     *
     * @throws ResourceNotFoundException if the module does not exist
     */
    public ModuleDetailResponse getById(UUID id) {
        log.debug("Loading managed module id={}", id);
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module", id));

        List<LessonSummary> lessons = moduleLessonRepository.findByModuleIdOrderBySortOrder(id)
                .stream()
                .map(ml -> new LessonSummary(ml.getLesson().getId(), ml.getLesson().getTitle()))
                .toList();

        ModuleDetailResponse response = toDetailResponse(module, lessons);
        log.debug("Loaded managed module id={} with {} lessons", id, lessons.size());
        return response;
    }

    /**
     * Updates all fields of an existing module, including its status.
     *
     * @throws ResourceNotFoundException if the module does not exist
     */
    @Transactional
    public ModuleResponse update(UUID id, UpdateModuleRequest request) {
        log.info("Updating module id={} title={} status={}", id, request.getTitle(), request.getStatus());
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module", id));

        module.setTitle(request.getTitle());
        module.setDescription(request.getDescription());
        module.setThumbnailUrl(request.getThumbnailUrl());
        module.setIfFreePreview(request.isIfFreePreview());
        module.setStatus(request.getStatus());

        ModuleResponse response = ModuleResponse.from(moduleRepository.save(module));
        log.info("Updated module id={} status={}", response.getId(), response.getStatus());
        return response;
    }

    /**
     * Deletes a module and all its module–lesson link records.
     *
     * @throws ResourceNotFoundException if the module does not exist
     */
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting module id={}", id);
        if (!moduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Module", id);
        }
        moduleLessonRepository.deleteByModuleId(id);
        moduleRepository.deleteById(id);
        log.info("Deleted module id={}", id);
    }

    private ModuleDetailResponse toDetailResponse(Module module, List<LessonSummary> lessons) {
        return new ModuleDetailResponse(
                module.getId(),
                module.getTitle(),
                module.getDescription(),
                module.getThumbnailUrl(),
                module.getStatus(),
                module.getSortOrder(),
                module.isIfFreePreview(),
                module.getCreatedAt(),
                module.getUpdatedAt(),
                lessons
        );
    }
}
