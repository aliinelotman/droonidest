package com.app.backend.service;

import com.app.backend.dto.response.LessonResponse;
import com.app.backend.exception.ResourceNotFoundException;
import com.app.backend.model.Lesson;
import com.app.backend.model.ModuleLesson;
import com.app.backend.model.enums.ContentStatus;
import com.app.backend.repository.ModuleLessonRepository;
import com.app.backend.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonService {

    private final ModuleLessonRepository moduleLessonRepository;
    private final ModuleRepository moduleRepository;

    public List<LessonResponse> getByModuleId(UUID moduleId) {
        moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", moduleId));

        return moduleLessonRepository.findByModuleIdAndLessonStatus(moduleId, ContentStatus.PUBLISHED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private LessonResponse toResponse(ModuleLesson ml) {
        Lesson lesson = ml.getLesson();
        return new LessonResponse(
                lesson.getId(),
                ml.getId().getModuleId(),
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
