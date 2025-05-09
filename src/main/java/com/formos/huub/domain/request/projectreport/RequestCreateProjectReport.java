package com.formos.huub.domain.request.projectreport;

import com.formos.huub.domain.request.common.RequestAttachmentFile;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import java.util.List;
import lombok.*;

@Getter
@Setter
public class RequestCreateProjectReport {

    @UUIDCheck
    private String projectId;

    @RequireCheck
    private String description;

    private List<String> serviceOutcomes;

    @RequireCheck
    private String feedback;

    private List<RequestAttachmentFile> attachments;

    private int hoursCompleted;

    private Boolean confirmation;
}
