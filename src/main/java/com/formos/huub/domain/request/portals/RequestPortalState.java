package com.formos.huub.domain.request.portals;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestPortalState {

    private UUID id;

    private String countryCode;

    @NotNull
    private RequestLocation state;

    @NotNull
    @NotEmpty
    private List<RequestLocation> cities;

    @NotNull
    @NotEmpty
    private List<RequestLocation> zipcodes;

    @NotNull
    private Integer priorityOrder;
}
