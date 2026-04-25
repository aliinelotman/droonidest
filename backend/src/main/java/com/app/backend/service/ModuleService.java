package com.app.backend.service;

import com.app.backend.dto.response.ModuleResponse;
import com.app.backend.exception.ResourceNotFoundException;
import com.app.backend.model.Module;
import com.app.backend.model.enums.ContentStatus;
import com.app.backend.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ModuleService {

    private final ModuleRepository moduleRepository;

    public List<ModuleResponse> getAll() {
        List<ModuleResponse> modules = moduleRepository.findAllByStatusOrderBySortOrder(ContentStatus.PUBLISHED)
                .stream()
                .map(ModuleResponse::from)
                .toList();
        log.debug("Loaded {} published modules", modules.size());
        return modules;
    }

    public ModuleResponse getById(UUID id) {
        log.debug("Loading published module id={}", id);
        Module module = moduleRepository.findById(id)
                .filter(m -> m.getStatus() == ContentStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Module", id));
        log.debug("Loaded published module id={}", id);
        return ModuleResponse.from(module);
    }
}
