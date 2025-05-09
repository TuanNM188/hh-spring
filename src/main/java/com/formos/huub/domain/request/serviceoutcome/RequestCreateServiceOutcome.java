package com.formos.huub.domain.request.serviceoutcome;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestCreateServiceOutcome {

    @NotNull
    @Size(max = 255)
    private String name;

    private String description;
}
