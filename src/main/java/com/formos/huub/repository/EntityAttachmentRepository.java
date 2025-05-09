package com.formos.huub.repository;

import com.formos.huub.domain.entity.EntityAttachment;
import com.formos.huub.domain.enums.EntityTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EntityAttachmentRepository extends JpaRepository<EntityAttachment, UUID> {

    List<EntityAttachment> findByEntityIdAndEntityType(UUID entityId, EntityTypeEnum entityType);

}
