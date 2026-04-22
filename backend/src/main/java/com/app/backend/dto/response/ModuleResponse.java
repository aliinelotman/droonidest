package com.app.backend.dto.response;

import com.app.backend.model.Module;
import com.app.backend.model.enums.ContentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ModuleResponse {

    private UUID id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private ContentStatus status;
    private int sortOrder;
    private boolean ifFreePreview;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    /**
     * Creates a response DTO from a Module entity.
     */
    public static ModuleResponse from(Module module) {
        return new ModuleResponse(
                module.getId(),
                module.getTitle(),
                module.getDescription(),
                module.getThumbnailUrl(),
                module.getStatus(),
                module.getSortOrder(),
                module.isIfFreePreview(),
                module.getCreatedAt(),
                module.getUpdatedAt()
        );
    }
}
