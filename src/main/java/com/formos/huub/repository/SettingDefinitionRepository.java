package com.formos.huub.repository;

import com.formos.huub.domain.entity.SettingDefinition;
import java.util.Optional;
import java.util.UUID;

import com.formos.huub.domain.enums.SettingKeyCodeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingDefinitionRepository extends JpaRepository<SettingDefinition, UUID> {
    Optional<SettingDefinition> findByKeyCode(SettingKeyCodeEnum keyCode);
}
