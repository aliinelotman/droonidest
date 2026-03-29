package com.app.backend.dto.response;

import com.app.backend.model.enums.ContentFormat;
import com.app.backend.model.enums.ContentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class LessonResponse {

    private UUID id;
    private UUID moduleId;
    private String title;
    private String content;
    private ContentStatus status;
    private ContentFormat contentFormat;
    private String videoUrl;
    private boolean ifFreePreview;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
