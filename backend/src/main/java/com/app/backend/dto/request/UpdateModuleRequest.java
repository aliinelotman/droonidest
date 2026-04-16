package com.app.backend.dto.request;

import com.app.backend.model.enums.ContentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateModuleRequest {

    @NotBlank(message = "Title must not be blank")
    private String title;

    private String description;
    private String thumbnailUrl;
    private boolean ifFreePreview;

    @NotNull(message = "Status must not be null")
    private ContentStatus status;
}
