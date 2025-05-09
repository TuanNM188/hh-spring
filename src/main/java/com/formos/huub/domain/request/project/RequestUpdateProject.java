package com.formos.huub.domain.request.project;

import com.formos.huub.domain.request.common.RequestAttachmentFile;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class RequestUpdateProject {

    private UUID id;

    @RequireCheck
    private String projectName;

    private UUID categoryId;

    private UUID serviceId;

    private Float estimatedHoursNeeded;

    private Instant proposedStartDate;

    private Instant estimatedCompletionDate;

    @RequireCheck
    private String scopeOfWork;

    @RequireCheck
    private String status;

    private List<RequestAttachmentFile> attachments;

}
