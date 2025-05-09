package com.formos.huub.domain.request.categories;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestCreateCategory {

    @NotNull
    @Size(max = 100)
    private String name;

    private String description;

    private String iconUrl;
}
