package com.formos.huub.service.clientnote;

import com.formos.huub.domain.constant.BusinessConstant;
import com.formos.huub.domain.entity.ClientNote;
import com.formos.huub.domain.enums.EntityTypeEnum;
import com.formos.huub.domain.request.clientnote.RequestSearchClientNotes;
import com.formos.huub.domain.request.clientnote.RequestCreateClientNote;
import com.formos.huub.domain.response.clientnote.ResponseClientNote;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.mapper.clientnote.ClientNoteMapper;
import com.formos.huub.mapper.entityattachment.EntityAttachmentMapper;
import com.formos.huub.repository.ClientNoteRepository;
import com.formos.huub.repository.EntityAttachmentRepository;
import com.formos.huub.service.businessowner.BusinessOwnerService;
import com.formos.huub.service.entityattachment.EntityAttachmentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ClientNoteService {

    ClientNoteMapper clientNoteMapper;
    ClientNoteRepository clientNoteRepository;
    EntityAttachmentService entityAttachmentService;
    BusinessOwnerService businessOwnerService;
    EntityAttachmentRepository entityAttachmentRepository;
    EntityAttachmentMapper entityAttachmentMapper;


    public UUID createClientNote(@Valid RequestCreateClientNote request) {

        var businessOwner = businessOwnerService.validateIsBusinessOwner(UUID.fromString(request.getUserId()));

        var clientNote = clientNoteMapper.toEntity(request, businessOwner.getId());

        clientNoteRepository.save(clientNote);

        entityAttachmentService.saveAllAttachment(request.getAttachments(), clientNote.getId());

        return clientNote.getId();
    }

    public Map<String, Object> searchClientNotesByUser(String userId, RequestSearchClientNotes request) {
        var businessOwner = businessOwnerService.validateIsBusinessOwner(UUID.fromString(userId));
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "createdDate,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        HashMap<String, String> sortMap = new HashMap<>();
        sortMap.put(BusinessConstant.TIMEZONE_KEY, request.getTimezone());
        request.setSearchConditions(ObjectUtils.convertSortParams(request.getSearchConditions(), sortMap));
        var data = clientNoteRepository.searchClientNotesByBusinessOwnerId(businessOwner.getId(), request, pageable);
        return PageUtils.toPage(data);
    }

    public ResponseClientNote getClientNoteById(String clientNoteId) {
        var clientNote = validateIsClientNote(UUID.fromString(clientNoteId));
        var attachments = entityAttachmentRepository.findByEntityIdAndEntityType(clientNote.getId(), EntityTypeEnum.CLIENT_NOTE);
        var responseClientNote = clientNoteMapper.toResponse(clientNote);
        var responseAttachments = entityAttachmentMapper.toResponseAttachments(attachments);
        responseClientNote.setAttachments(responseAttachments);
        return responseClientNote;
    }

    private ClientNote validateIsClientNote(UUID userId) {
        return clientNoteRepository
            .findById(userId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Client Note")));
    }

}
