package com.formos.huub.domain.request.project;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class RequestCreateProjectExtension {

    private UUID projectId;

    private Instant newCompletionDate;

    @RequireCheck
    private String requestExplanation;

}
