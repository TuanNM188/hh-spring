package com.formos.huub.domain.response.funding;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseFunder {

    private UUID id;

    private String name;

    private String funderUrl;

    private String description;

    private String imageUrl;
}
