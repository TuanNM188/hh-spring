package com.formos.huub.domain.response.project;

import com.formos.huub.domain.enums.ProjectStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ResponseSearchProject {

    private UUID id;

    private String projectName;

    private Float estimatedHoursNeeded;

    private Instant completedDate;

    private UUID businessOwnerId;

    private String businessOwnerName;

    private UUID advisorId;

    private String advisorName;

    private UUID navigatorId;

    private String navigatorName;

    private ProjectStatusEnum status;

    private Instant requestDate;

    private Instant estimatedCompletionDate;
}
