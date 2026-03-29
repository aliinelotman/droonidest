package com.app.backend.model;

import com.app.backend.model.enums.ContentFormat;
import com.app.backend.model.enums.ContentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ContentStatus status = ContentStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_format", nullable = false, length = 50)
    private ContentFormat contentFormat = ContentFormat.MARKDOWN;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "if_free_preview", nullable = false)
    private boolean ifFreePreview;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @Setter(AccessLevel.NONE)
    private OffsetDateTime updatedAt;

    public Lesson(String title) {
        this.title = title;
    }
}
