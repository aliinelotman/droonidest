package com.app.backend.repository;

import com.app.backend.model.ModuleLesson;
import com.app.backend.model.ModuleLessonId;
import com.app.backend.model.enums.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ModuleLessonRepository extends JpaRepository<ModuleLesson, ModuleLessonId> {

    @Query("SELECT ml FROM ModuleLesson ml " +
           "JOIN FETCH ml.lesson l " +
           "WHERE ml.module.id = :moduleId AND l.status = :lessonStatus " +
           "ORDER BY ml.sortOrder")
    List<ModuleLesson> findByModuleIdAndLessonStatus(
            @Param("moduleId") UUID moduleId,
            @Param("lessonStatus") ContentStatus lessonStatus);

    @Query("SELECT ml FROM ModuleLesson ml " +
           "JOIN FETCH ml.lesson l " +
           "WHERE ml.module.id = :moduleId " +
           "ORDER BY ml.sortOrder")
    List<ModuleLesson> findByModuleIdOrderBySortOrder(@Param("moduleId") UUID moduleId);

    @Query("SELECT ml FROM ModuleLesson ml " +
           "JOIN FETCH ml.lesson l " +
           "WHERE l.id = :lessonId")
    List<ModuleLesson> findByLessonId(@Param("lessonId") UUID lessonId);

    long countByModuleId(UUID moduleId);

    void deleteByModuleId(UUID moduleId);

    void deleteByLessonId(UUID lessonId);
}
