package com.formos.huub.domain.request.projectreport;

import com.formos.huub.domain.request.common.RequestAttachmentFile;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RequestUpdateProjectReport {

    @UUIDCheck
    private String Id;

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
