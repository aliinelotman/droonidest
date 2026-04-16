package com.app.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateModuleRequest {

    @NotBlank(message = "Title must not be blank")
    private String title;

    private String description;
    private String thumbnailUrl;
    private boolean ifFreePreview;
}
