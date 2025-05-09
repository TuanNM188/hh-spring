package com.formos.huub.domain.request.specialty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestSpecialtyForTA {

    @NotNull
    private UUID id;

    @NotNull
    @Min(value = 0)
    private Integer yearsOfExperience;

    private List<UUID> specialtyAreaIds;
}
