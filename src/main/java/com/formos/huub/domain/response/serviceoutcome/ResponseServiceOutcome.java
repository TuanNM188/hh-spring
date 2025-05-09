package com.formos.huub.domain.response.serviceoutcome;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseServiceOutcome {

    private UUID id;

    private String name;

    private String description;
}
