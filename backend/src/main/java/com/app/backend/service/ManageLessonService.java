package com.app.backend.service;

import com.app.backend.dto.request.CreateLessonRequest;
import com.app.backend.dto.request.UpdateLessonRequest;
import com.app.backend.dto.response.LessonResponse;
import com.app.backend.exception.ResourceNotFoundException;
import com.app.backend.model.Lesson;
import com.app.backend.model.Module;
import com.app.backend.model.ModuleLesson;
import com.app.backend.model.enums.ContentFormat;
import com.app.backend.repository.LessonRepository;
import com.app.backend.repository.ModuleLessonRepository;
import com.app.backend.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Business logic for content-management lesson operations (CRUD).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ManageLessonService {

    private final LessonRepository lessonRepository;
    private final ModuleRepository moduleRepository;
    private final ModuleLessonRepository moduleLessonRepository;

    /**
     * Creates a new lesson, links it to the given module, and auto-assigns sort order.
     *
     * @throws ResourceNotFoundException if the module does not exist
     */
    @Transactional
    public LessonResponse create(UUID moduleId, CreateLessonRequest request) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", moduleId));

        Lesson lesson = new Lesson(request.getTitle());
        lesson.setContent(request.getContent());
        lesson.setContentFormat(request.getContentFormat() != null ? request.getContentFormat() : ContentFormat.MARKDOWN);
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setIfFreePreview(request.isIfFreePreview());
        lesson = lessonRepository.save(lesson);

        int nextSortOrder = (int) moduleLessonRepository.countByModuleId(moduleId);
        ModuleLesson link = new ModuleLesson(module, lesson, nextSortOrder);
        moduleLessonRepository.save(link);

        LessonResponse response = toResponse(lesson, moduleId);
        log.info("Created lesson id={} moduleId={} status={} sortOrder={}",
                response.getId(), moduleId, response.getStatus(), nextSortOrder);
        return response;
    }

    /**
     * Returns a lesson with full content and its parent module ID.
     *
     * @throws ResourceNotFoundException if the lesson does not exist
     */
    public LessonResponse getById(UUID id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", id));

        List<ModuleLesson> links = moduleLessonRepository.findByLessonId(id);
        UUID moduleId = links.isEmpty() ? null : links.getFirst().getId().getModuleId();

        return toResponse(lesson, moduleId);
    }

    /**
     * Updates all mutable fields of an existing lesson.
     *
     * @throws ResourceNotFoundException if the lesson does not exist
     */
    @Transactional
    public LessonResponse update(UUID id, UpdateLessonRequest request) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", id));

        lesson.setTitle(request.getTitle());
        lesson.setContent(request.getContent());
        lesson.setContentFormat(request.getContentFormat() != null ? request.getContentFormat() : ContentFormat.MARKDOWN);
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setIfFreePreview(request.isIfFreePreview());
        lesson = lessonRepository.save(lesson);

        List<ModuleLesson> links = moduleLessonRepository.findByLessonId(id);
        UUID moduleId = links.isEmpty() ? null : links.getFirst().getId().getModuleId();

        LessonResponse response = toResponse(lesson, moduleId);
        log.info("Updated lesson id={} moduleId={} status={}", response.getId(), moduleId, response.getStatus());
        return response;
    }

    /**
     * Deletes a lesson and all its module–lesson link records.
     *
     * @throws ResourceNotFoundException if the lesson does not exist
     */
    @Transactional
    public void delete(UUID id) {
        if (!lessonRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lesson", id);
        }
        moduleLessonRepository.deleteByLessonId(id);
        lessonRepository.deleteById(id);
        log.info("Deleted lesson id={}", id);
    }

    private LessonResponse toResponse(Lesson lesson, UUID moduleId) {
        return new LessonResponse(
                lesson.getId(),
                moduleId,
                lesson.getTitle(),
                lesson.getContent(),
                lesson.getStatus(),
                lesson.getContentFormat(),
                lesson.getVideoUrl(),
                lesson.isIfFreePreview(),
                lesson.getCreatedAt(),
                lesson.getUpdatedAt()
        );
    }
}
