package com.app.backend.service;

import com.app.backend.dto.response.ModuleResponse;
import com.app.backend.exception.ResourceNotFoundException;
import com.app.backend.model.Module;
import com.app.backend.model.enums.ContentStatus;
import com.app.backend.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModuleService {

    private final ModuleRepository moduleRepository;

    public List<ModuleResponse> getAll() {
        return moduleRepository.findAllByStatusOrderBySortOrder(ContentStatus.PUBLISHED)
                .stream()
                .map(ModuleResponse::from)
                .toList();
    }

    public ModuleResponse getById(UUID id) {
        Module module = moduleRepository.findById(id)
                .filter(m -> m.getStatus() == ContentStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Module", id));
        return ModuleResponse.from(module);
    }
}
