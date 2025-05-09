package com.formos.huub.domain.response.project;

import com.formos.huub.domain.enums.ProjectStatusEnum;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResponseProjectForBusinessOwner {

    UUID projectId;
    UUID technicalAdvisorId;
    Instant estimatedCompletionDate;
    String projectName;
    String technicalAdvisorName;
    ProjectStatusEnum status;
}
