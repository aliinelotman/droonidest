package com.app.backend.service;

import com.app.backend.TestEntityFactory;
import com.app.backend.dto.request.CreateModuleRequest;
import com.app.backend.dto.request.UpdateModuleRequest;
import com.app.backend.dto.response.ModuleDetailResponse;
import com.app.backend.dto.response.ModuleResponse;
import com.app.backend.exception.ResourceNotFoundException;
import com.app.backend.model.Lesson;
import com.app.backend.model.Module;
import com.app.backend.model.ModuleLesson;
import com.app.backend.model.enums.ContentStatus;
import com.app.backend.repository.ModuleLessonRepository;
import com.app.backend.repository.ModuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageModuleServiceTest {

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private ModuleLessonRepository moduleLessonRepository;

    private ManageModuleService service;

    private static final UUID MODULE_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @BeforeEach
    void setUp() {
        service = new ManageModuleService(moduleRepository, moduleLessonRepository);
    }

    @Test
    void testGivenValidRequestWhenCreateThenReturnModuleWithAutoSortOrder() {
        when(moduleRepository.count()).thenReturn(3L);
        Module saved = TestEntityFactory.createModule(MODULE_ID, "New Module", 3);
        when(moduleRepository.save(any(Module.class))).thenReturn(saved);

        CreateModuleRequest request = new CreateModuleRequest("New Module", "desc", null, false);
        ModuleResponse result = service.create(request);

        assertThat(result.getTitle()).isEqualTo("New Module");
        assertThat(result.getSortOrder()).isEqualTo(3);

        ArgumentCaptor<Module> captor = ArgumentCaptor.forClass(Module.class);
        verify(moduleRepository).save(captor.capture());
        assertThat(captor.getValue().getSortOrder()).isEqualTo(3);
    }

    @Test
    void testWhenGetAllThenReturnAllModulesOrdered() {
        Module m1 = TestEntityFactory.createModule(UUID.randomUUID(), "First", 0);
        Module m2 = TestEntityFactory.createModule(UUID.randomUUID(), "Second", 1);
        m2.setStatus(ContentStatus.DRAFT);
        when(moduleRepository.findAllByOrderBySortOrder()).thenReturn(List.of(m1, m2));

        List<ModuleResponse> result = service.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("First");
        assertThat(result.get(1).getTitle()).isEqualTo("Second");
    }

    @Test
    void testGivenExistingIdWhenGetByIdThenReturnModuleWithLessons() {
        Module module = TestEntityFactory.createModule(MODULE_ID, "Test Module", 0);
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));

        Lesson lesson = TestEntityFactory.createLesson(UUID.randomUUID(), "Lesson 1");
        ModuleLesson ml = new ModuleLesson(module, lesson, 0);
        when(moduleLessonRepository.findByModuleIdOrderBySortOrder(MODULE_ID)).thenReturn(List.of(ml));

        ModuleDetailResponse result = service.getById(MODULE_ID);

        assertThat(result.getTitle()).isEqualTo("Test Module");
        assertThat(result.getLessons()).hasSize(1);
        assertThat(result.getLessons().getFirst().title()).isEqualTo("Lesson 1");
    }

    @Test
    void testGivenMissingIdWhenGetByIdThenThrow404() {
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(MODULE_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testGivenValidRequestWhenUpdateThenSetFieldsIncludingStatus() {
        Module module = TestEntityFactory.createModule(MODULE_ID, "Old Title", 0);
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));
        when(moduleRepository.save(any(Module.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateModuleRequest request = new UpdateModuleRequest(
                "New Title", "new desc", "thumb.png", true, ContentStatus.PUBLISHED);
        ModuleResponse result = service.update(MODULE_ID, request);

        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getStatus()).isEqualTo(ContentStatus.PUBLISHED);
        assertThat(result.isIfFreePreview()).isTrue();
    }

    @Test
    void testGivenMissingIdWhenUpdateThenThrow404() {
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.empty());
        UpdateModuleRequest request = new UpdateModuleRequest(
                "Title", null, null, false, ContentStatus.DRAFT);

        assertThatThrownBy(() -> service.update(MODULE_ID, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testGivenExistingIdWhenDeleteThenRemoveLinksAndModule() {
        when(moduleRepository.existsById(MODULE_ID)).thenReturn(true);

        service.delete(MODULE_ID);

        verify(moduleLessonRepository).deleteByModuleId(MODULE_ID);
        verify(moduleRepository).deleteById(MODULE_ID);
    }

    @Test
    void testGivenMissingIdWhenDeleteThenThrow404() {
        when(moduleRepository.existsById(MODULE_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(MODULE_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
