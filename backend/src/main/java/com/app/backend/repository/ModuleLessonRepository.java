package com.app.backend.repository;

import com.app.backend.model.ModuleLesson;
import com.app.backend.model.ModuleLessonId;
import com.app.backend.model.enums.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ModuleLessonRepository extends JpaRepository<ModuleLesson, ModuleLessonId> {

    @Query("SELECT ml FROM ModuleLesson ml " +
           "JOIN FETCH ml.lesson l " +
           "WHERE ml.module.id = :moduleId AND l.status = :status " +
           "ORDER BY ml.sortOrder")
    List<ModuleLesson> findByModuleIdAndLessonStatus(UUID moduleId, ContentStatus status);
}
