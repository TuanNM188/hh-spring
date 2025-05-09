package com.formos.huub.domain.response.specialty;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseSpecialty {

    private UUID id;

    private String name;

    private String description;
}
