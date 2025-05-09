package com.formos.huub.domain.response.project;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ResponseProjectExtensionRequest {

    private UUID id;

    private UUID projectId;

    private Instant newCompletionDate;

    private String requestExplanation;
}
