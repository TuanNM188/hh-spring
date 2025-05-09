package com.formos.huub.domain.request.portals;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestPortalLocation {

    private RequestLocation country;

    @Valid
    private List<RequestPortalState> states;

}
