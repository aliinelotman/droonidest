package com.app.backend.dto.response;

import com.app.backend.model.enums.ContentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ModuleDetailResponse {

    private UUID id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private ContentStatus status;
    private int sortOrder;
    private boolean ifFreePreview;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<LessonSummary> lessons;

    public record LessonSummary(UUID id, String title) {
    }
}
