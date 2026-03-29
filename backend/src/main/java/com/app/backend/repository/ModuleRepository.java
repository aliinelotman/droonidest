package com.app.backend.repository;

import com.app.backend.model.Module;
import com.app.backend.model.enums.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ModuleRepository extends JpaRepository<Module, UUID> {

    List<Module> findAllByStatusOrderBySortOrder(ContentStatus status);
}
