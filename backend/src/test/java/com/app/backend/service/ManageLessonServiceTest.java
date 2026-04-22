package com.app.backend.service;

import com.app.backend.TestEntityFactory;
import com.app.backend.dto.request.CreateLessonRequest;
import com.app.backend.dto.request.UpdateLessonRequest;
import com.app.backend.dto.response.LessonResponse;
import com.app.backend.exception.ResourceNotFoundException;
import com.app.backend.model.Lesson;
import com.app.backend.model.Module;
import com.app.backend.model.ModuleLesson;
import com.app.backend.model.enums.ContentFormat;
import com.app.backend.model.enums.ContentStatus;
import com.app.backend.repository.LessonRepository;
import com.app.backend.repository.ModuleLessonRepository;
import com.app.backend.repository.ModuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageLessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private ModuleLessonRepository moduleLessonRepository;

    private ManageLessonService service;

    private static final UUID MODULE_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID LESSON_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @BeforeEach
    void setUp() {
        service = new ManageLessonService(lessonRepository, moduleRepository, moduleLessonRepository);
    }

    @Test
    void testGivenValidRequestWhenCreateThenReturnLessonWithAutoSortOrder() {
        Module module = TestEntityFactory.createModule(MODULE_ID, "Module", 0);
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));

        Lesson savedLesson = TestEntityFactory.createLesson(LESSON_ID, "New Lesson");
        savedLesson.setContent("# Hello");
        when(lessonRepository.save(any(Lesson.class))).thenReturn(savedLesson);

        when(moduleLessonRepository.countByModuleId(MODULE_ID))
                .thenReturn(0L);
        when(moduleLessonRepository.save(any(ModuleLesson.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CreateLessonRequest request = new CreateLessonRequest(
                "New Lesson", "# Hello", ContentFormat.MARKDOWN, null, false);
        LessonResponse result = service.create(MODULE_ID, request);

        assertThat(result.getTitle()).isEqualTo("New Lesson");
        assertThat(result.getModuleId()).isEqualTo(MODULE_ID);
        assertThat(result.getStatus()).isEqualTo(ContentStatus.DRAFT);
        verify(moduleLessonRepository).save(any(ModuleLesson.class));
    }

    @Test
    void testGivenMissingModuleWhenCreateThenThrow404() {
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.empty());

        CreateLessonRequest request = new CreateLessonRequest("Title", null, null, null, false);

        assertThatThrownBy(() -> service.create(MODULE_ID, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testGivenExistingIdWhenGetByIdThenReturnFullLesson() {
        Lesson lesson = TestEntityFactory.createLesson(LESSON_ID, "My Lesson");
        lesson.setContent("Some content");
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lesson));

        Module module = TestEntityFactory.createModule(MODULE_ID, "Module", 0);
        ModuleLesson ml = new ModuleLesson(module, lesson, 0);
        when(moduleLessonRepository.findByLessonId(LESSON_ID)).thenReturn(List.of(ml));

        LessonResponse result = service.getById(LESSON_ID);

        assertThat(result.getTitle()).isEqualTo("My Lesson");
        assertThat(result.getContent()).isEqualTo("Some content");
        assertThat(result.getModuleId()).isEqualTo(MODULE_ID);
    }

    @Test
    void testGivenMissingIdWhenGetByIdThenThrow404() {
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(LESSON_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testGivenValidRequestWhenUpdateThenSetFields() {
        Lesson lesson = TestEntityFactory.createLesson(LESSON_ID, "Old");
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));
        when(moduleLessonRepository.findByLessonId(LESSON_ID)).thenReturn(Collections.emptyList());

        UpdateLessonRequest request = new UpdateLessonRequest(
                "Updated", "new content", ContentFormat.HTML, "https://video.url", true);
        LessonResponse result = service.update(LESSON_ID, request);

        assertThat(result.getTitle()).isEqualTo("Updated");
        assertThat(result.getContent()).isEqualTo("new content");
        assertThat(result.getContentFormat()).isEqualTo(ContentFormat.HTML);
        assertThat(result.isIfFreePreview()).isTrue();
    }

    @Test
    void testGivenMissingIdWhenUpdateThenThrow404() {
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.empty());
        UpdateLessonRequest request = new UpdateLessonRequest("T", null, null, null, false);

        assertThatThrownBy(() -> service.update(LESSON_ID, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testGivenExistingIdWhenDeleteThenRemoveLinksAndLesson() {
        when(lessonRepository.existsById(LESSON_ID)).thenReturn(true);

        service.delete(LESSON_ID);

        verify(moduleLessonRepository).deleteByLessonId(LESSON_ID);
        verify(lessonRepository).deleteById(LESSON_ID);
    }

    @Test
    void testGivenMissingIdWhenDeleteThenThrow404() {
        when(lessonRepository.existsById(LESSON_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(LESSON_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
