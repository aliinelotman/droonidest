package com.app.backend.model;

import com.app.backend.model.enums.LessonProgressStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_lesson_progress")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLessonProgress {

    @EmbeddedId
    @Setter(AccessLevel.NONE)
    private UserLessonProgressId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    @Setter(AccessLevel.NONE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("lessonId")
    @JoinColumn(name = "lesson_id")
    @Setter(AccessLevel.NONE)
    private Lesson lesson;

    @Column(name = "progress_pct", precision = 5, scale = 2)
    private BigDecimal progressPct = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "lesson_progress_status")
    private LessonProgressStatus status = LessonProgressStatus.NOT_STARTED;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "last_viewed_at")
    private OffsetDateTime lastViewedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @Setter(AccessLevel.NONE)
    private OffsetDateTime updatedAt;

    public UserLessonProgress(User user, Lesson lesson) {
        this.id = new UserLessonProgressId(user.getId(), lesson.getId());
        this.user = user;
        this.lesson = lesson;
    }
}
