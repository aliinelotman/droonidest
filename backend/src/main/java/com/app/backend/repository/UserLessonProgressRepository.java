package com.app.backend.repository;

import com.app.backend.model.UserLessonProgress;
import com.app.backend.model.UserLessonProgressId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserLessonProgressRepository extends JpaRepository<UserLessonProgress, UserLessonProgressId> {

    List<UserLessonProgress> findByUserId(UUID userId);

    Optional<UserLessonProgress> findByUserIdAndLessonId(UUID userId, UUID lessonId);
}
