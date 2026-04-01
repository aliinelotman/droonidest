package com.app.backend.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "module_lessons")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ModuleLesson {

    @EmbeddedId
    @Setter(AccessLevel.NONE)
    private ModuleLessonId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("moduleId")
    @JoinColumn(name = "module_id")
    @Setter(AccessLevel.NONE)
    private Module module;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("lessonId")
    @JoinColumn(name = "lesson_id")
    @Setter(AccessLevel.NONE)
    private Lesson lesson;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private OffsetDateTime createdAt;

    public ModuleLesson(Module module, Lesson lesson, int sortOrder) {
        this.id = new ModuleLessonId(module.getId(), lesson.getId());
        this.module = module;
        this.lesson = lesson;
        this.sortOrder = sortOrder;
    }
}
