package com.formos.huub.domain.request.specialty;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestCreateSpecialtyArea {

    @NotNull
    @Size(max = 100)
    private String name;

    private String description;
}
