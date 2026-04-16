package com.app.backend.service;

import com.app.backend.TestEntityFactory;
import com.app.backend.dto.response.ModuleResponse;
import com.app.backend.exception.ResourceNotFoundException;
import com.app.backend.model.Module;
import com.app.backend.model.enums.ContentStatus;
import com.app.backend.repository.ModuleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ModuleService}.
 */
@ExtendWith(MockitoExtension.class)
class ModuleServiceTest {

    @Mock
    private ModuleRepository moduleRepository;

    @InjectMocks
    private ModuleService moduleService;

    @Test
    void getAll_shouldReturnPublishedModulesOrderedBySortOrder() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        Module module1 = createModule(id1, "Module 1", "Description 1", "thumb1.jpg", ContentStatus.PUBLISHED, 1, true, now, now);
        Module module2 = createModule(id2, "Module 2", "Description 2", null, ContentStatus.PUBLISHED, 2, false, now, now);

        when(moduleRepository.findAllByStatusOrderBySortOrder(ContentStatus.PUBLISHED))
                .thenReturn(List.of(module1, module2));

        List<ModuleResponse> result = moduleService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(id1);
        assertThat(result.get(0).getTitle()).isEqualTo("Module 1");
        assertThat(result.get(0).getDescription()).isEqualTo("Description 1");
        assertThat(result.get(0).getThumbnailUrl()).isEqualTo("thumb1.jpg");
        assertThat(result.get(0).getStatus()).isEqualTo(ContentStatus.PUBLISHED);
        assertThat(result.get(0).getSortOrder()).isEqualTo(1);
        assertThat(result.get(0).isIfFreePreview()).isTrue();
        assertThat(result.get(0).getCreatedAt()).isEqualTo(now);
        assertThat(result.get(0).getUpdatedAt()).isEqualTo(now);

        assertThat(result.get(1).getId()).isEqualTo(id2);
        assertThat(result.get(1).getTitle()).isEqualTo("Module 2");
        assertThat(result.get(1).getThumbnailUrl()).isNull();
    }

    @Test
    void getById_shouldReturnModuleResponse_whenModuleExistsAndPublished() {
        // Given
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        Module module = createModule(id, "Module 1", "Description 1", "thumb1.jpg", ContentStatus.PUBLISHED, 1, true, now, now);

        when(moduleRepository.findById(id)).thenReturn(Optional.of(module));

        // When
        ModuleResponse result = moduleService.getById(id);

        // Then
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getTitle()).isEqualTo("Module 1");
        assertThat(result.getDescription()).isEqualTo("Description 1");
        assertThat(result.getThumbnailUrl()).isEqualTo("thumb1.jpg");
        assertThat(result.getStatus()).isEqualTo(ContentStatus.PUBLISHED);
        assertThat(result.getSortOrder()).isEqualTo(1);
        assertThat(result.isIfFreePreview()).isTrue();
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void getById_shouldThrowResourceNotFoundException_whenModuleNotFound() {
        // Given
        UUID id = UUID.randomUUID();
        when(moduleRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> moduleService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_shouldThrowResourceNotFoundException_whenModuleNotPublished() {
        // Given
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        Module module = createModule(id, "Module 1", "Description 1", "thumb1.jpg", ContentStatus.DRAFT, 1, true, now, now);

        when(moduleRepository.findById(id)).thenReturn(Optional.of(module));

        // When & Then
        assertThatThrownBy(() -> moduleService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Module createModule(UUID id, String title, String description, String thumbnailUrl,
                                ContentStatus status, int sortOrder, boolean ifFreePreview,
                                OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        Module module = new Module(title, sortOrder);
        setId(module, id);
        module.setDescription(description);
        module.setThumbnailUrl(thumbnailUrl);
        module.setStatus(status);
        module.setIfFreePreview(ifFreePreview);
        setCreatedAt(module, createdAt);
        setUpdatedAt(module, updatedAt);
        return module;
    }

    private void setId(Module module, UUID id) {
        try {
            Field field = Module.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(module, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set module ID for test", e);
        }
    }

    private void setCreatedAt(Module module, OffsetDateTime createdAt) {
        try {
            Field field = Module.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(module, createdAt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set createdAt for test", e);
        }
    }

    private void setUpdatedAt(Module module, OffsetDateTime updatedAt) {
        try {
            Field field = Module.class.getDeclaredField("updatedAt");
            field.setAccessible(true);
            field.set(module, updatedAt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set updatedAt for test", e);
        }
    }
}