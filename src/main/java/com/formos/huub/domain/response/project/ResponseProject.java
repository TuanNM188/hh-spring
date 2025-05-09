package com.formos.huub.domain.response.project;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.formos.huub.domain.entity.EntityAttachment;
import lombok.*;

@Getter
@Setter
public class ResponseProject {

    private UUID id;

    private String portalName;

    private String projectName;

    private String vendorName;

    private String categoryId;

    private UUID serviceId;

    private Float estimatedHoursNeeded;

    // remainingAwardHours from TechnicalAssistanceSubmit
    private Float remainingAwardHours;

    private Instant proposedStartDate;

    private Instant estimatedCompletionDate;

    private String status;

    private String scopeOfWork;

    private String categoryName;

    private Instant appointmentStartDate;

    private UUID appointmentId;

    private List<EntityAttachment> attachments;

}
