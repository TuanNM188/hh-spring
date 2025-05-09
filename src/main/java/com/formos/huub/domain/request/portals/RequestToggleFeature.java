package com.formos.huub.domain.request.portals;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestToggleFeature {
    @NotNull
    private UUID featureId;

    @NotNull
    private Boolean isActive;
}
