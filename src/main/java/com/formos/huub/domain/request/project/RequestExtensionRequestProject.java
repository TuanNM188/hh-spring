package com.formos.huub.domain.request.project;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class RequestExtensionRequestProject {

    private UUID id;

    private UUID projectId;

    private Instant newCompletionDate;

    private String requestExplanation;

}
