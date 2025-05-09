package com.formos.huub.service.entityattachment;

import com.formos.huub.domain.entity.EntityAttachment;
import com.formos.huub.domain.request.common.RequestAttachmentFile;
import com.formos.huub.mapper.entityattachment.EntityAttachmentMapper;
import com.formos.huub.repository.EntityAttachmentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class EntityAttachmentService {

    EntityAttachmentMapper entityAttachmentMapper;
    EntityAttachmentRepository entityAttachmentRepository;

    public void saveAllAttachment(List<RequestAttachmentFile> attachments, UUID clientNoteId) {
        List<EntityAttachment> attachmentEntities = Optional.ofNullable(attachments)
            .filter(a -> !a.isEmpty())
            .map(
                a ->
                    a.stream().map(file -> entityAttachmentMapper.toEntity(file, clientNoteId)).toList()
            )
            .orElse(Collections.emptyList());

        if (!attachmentEntities.isEmpty()) {
            entityAttachmentRepository.saveAll(attachmentEntities);
        }
    }

}
