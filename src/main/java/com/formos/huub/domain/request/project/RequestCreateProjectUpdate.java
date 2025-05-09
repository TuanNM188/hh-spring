package com.formos.huub.domain.request.project;

import com.formos.huub.domain.request.common.RequestAttachmentFile;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestCreateProjectUpdate {

    private UUID projectId;

    @RequireCheck
    private String description;

    private List<RequestAttachmentFile> attachmentFiles;
}


