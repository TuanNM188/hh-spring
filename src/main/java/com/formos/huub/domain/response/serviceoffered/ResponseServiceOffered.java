package com.formos.huub.domain.response.serviceoffered;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseServiceOffered {

    private UUID id;

    private String name;

    private String description;
}
