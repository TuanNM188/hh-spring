/**
 * ***************************************************
 * * Description :
 * * File        : RequestCreateFeedback
 * * Author      : Hung Tran
 * * Date        : Jan 20, 2025
 * ***************************************************
 **/
package com.formos.huub.domain.request.project;

import com.formos.huub.domain.request.common.RequestAttachmentFile;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestCreateProject {

    private UUID appointmentId;

    private UUID portalId;

    private UUID applicationId;

    @RequireCheck
    private String projectName;

    // Is Community Partner
    private UUID vendorId;

    private UUID categoryId;

    private UUID assignAdvisorId;

    private UUID serviceId;

    private Float estimatedHoursNeeded;

    private Instant proposedStartDate;

    private Instant estimatedCompletionDate;

    @RequireCheck
    private String scopeOfWork;

    private List<RequestAttachmentFile> attachments;
}
