package com.app.backend.repository;

import com.app.backend.model.ModuleLesson;
import com.app.backend.model.ModuleLessonId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ModuleLessonRepository extends JpaRepository<ModuleLesson, ModuleLessonId> {

    List<ModuleLesson> findByModuleIdOrderBySortOrder(UUID moduleId);
}
