package com.formos.huub.domain.response.project;

import com.formos.huub.domain.entity.EntityAttachment;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseProjectReport {

    private UUID id;

    private Integer hoursCompleted;

    private String description;

    private List<String> serviceOutcomes;

    private String feedback;

    private UUID projectId;

    private List<EntityAttachment> attachments;

    private Boolean confirmation;
}
