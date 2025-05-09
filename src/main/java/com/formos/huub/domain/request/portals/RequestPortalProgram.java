package com.formos.huub.domain.request.portals;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestPortalProgram {

    private UUID id;

    private String name;

    private Instant contractStart;

    private Instant contractEnd;

    private Boolean isActive;

    private UUID programManagerId;

    private @Valid List<RequestProgramTerm> programTermRequests;
}
