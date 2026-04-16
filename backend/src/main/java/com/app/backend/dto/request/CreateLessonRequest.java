package com.app.backend.dto.request;

import com.app.backend.model.enums.ContentFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateLessonRequest {

    @NotBlank(message = "Title must not be blank")
    private String title;

    private String content;
    private ContentFormat contentFormat;
    private String videoUrl;
    private boolean ifFreePreview;
}
