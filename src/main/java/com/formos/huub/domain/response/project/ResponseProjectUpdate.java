package com.formos.huub.domain.response.project;

import com.formos.huub.domain.entity.EntityAttachment;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseProjectUpdate {

    private UUID id;

    private UUID projectId;

    private String imageUrl;

    private String createdBy;

    private String description;

    private Instant createdDate;

    private List<EntityAttachment> attachmentFiles;

}
