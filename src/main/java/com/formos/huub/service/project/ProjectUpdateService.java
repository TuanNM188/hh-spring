package com.formos.huub.service.project;

import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.EntityTypeEnum;
import com.formos.huub.domain.request.common.RequestAttachmentFile;
import com.formos.huub.domain.request.project.RequestCreateProjectUpdate;
import com.formos.huub.domain.response.project.ResponseProjectUpdate;
import com.formos.huub.framework.exception.NotFoundException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.mapper.entityattachment.EntityAttachmentMapper;
import com.formos.huub.mapper.project.ProjectUpdateMapper;
import com.formos.huub.repository.*;
import com.formos.huub.security.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectUpdateService {

    private static final String PROJECT_UPDATE_CREATED_AUTOMATED = "Automated";

    private final ProjectUpdateRepository projectUpdateRepository;

    private final EntityAttachmentRepository entityAttachmentRepository;

    private final UserRepository userRepository;

    private final ProjectRepository projectRepository;

    private final ProjectUpdateMapper projectUpdateMapper;

    private final EntityAttachmentMapper entityAttachmentMapper;


    /**
     *
     * @param request RequestCreateProjectUpdate
     */
    public void create(RequestCreateProjectUpdate request) {

        var project = projectRepository.findById(request.getProjectId()).orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Project")));

        var currentUser = SecurityUtils.getCurrentUser(userRepository);

        ProjectUpdate projectUpdate = projectUpdateMapper.toEntity(request);
        projectUpdate.setUserId(currentUser.getId());
        ProjectUpdate projectUpdateSaved = projectUpdateRepository.save(projectUpdate);

        saveAttachments(request.getAttachmentFiles(), projectUpdateSaved.getId());

    }

    /**
     *
     * @param projectId UUID
     * @return List<ResponseProjectUpdate>
     */
    public List<ResponseProjectUpdate> findAllByOrderByCreatedDateDesc(UUID projectId) {
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        return projectUpdateRepository.findAllByProjectIdAndOrderByCreatedDateDesc(projectId)
            .stream()
            .map(projectUpdate -> {
                var attachments = entityAttachmentRepository.findByEntityIdAndEntityType(projectUpdate.getId(), EntityTypeEnum.PROJECT_UPDATE);
                User user = null;
                if (Objects.nonNull(projectUpdate.getUserId())) {
                     user = userRepository.findById(projectUpdate.getUserId()).orElse(null);
                }
                var response = projectUpdateMapper.toResponse(projectUpdate, Objects.nonNull(user) ? user.getImageUrl() : null, Objects.isNull(user) ? PROJECT_UPDATE_CREATED_AUTOMATED : user.getNormalizedFullName());
                response.setAttachmentFiles(attachments);
                return response;
            })
            .collect(Collectors.toList());
    }

    /**
     * Save attachments
     * @param attachmentsRequest List<RequestAttachmentFile>
     * @param projectId UUID
     */
    private void saveAttachments(List<RequestAttachmentFile> attachmentsRequest, UUID projectId) {
        if (attachmentsRequest != null && !attachmentsRequest.isEmpty()) {
            List<EntityAttachment> attachmentEntities = attachmentsRequest
                .stream()
                .map(file -> entityAttachmentMapper.toEntity(file, projectId))
                .toList();

            if (!attachmentEntities.isEmpty()) {
                entityAttachmentRepository.saveAll(attachmentEntities);
            }
        }
    }

}
