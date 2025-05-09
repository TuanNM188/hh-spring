package com.formos.huub.domain.response.portals;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponsePortalProgram {
    private UUID id;

    private String name;

    private Instant contractStart;

    private Instant contractEnd;

    private Boolean isActive;

    private List<ResponseProgramTerm> programTerms;
}
