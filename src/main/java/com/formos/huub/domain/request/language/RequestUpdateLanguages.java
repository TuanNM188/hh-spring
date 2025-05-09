package com.formos.huub.domain.request.language;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestUpdateLanguages {

    @NotNull
    @Size(max = 100)
    private String name;

    private String code;
}
