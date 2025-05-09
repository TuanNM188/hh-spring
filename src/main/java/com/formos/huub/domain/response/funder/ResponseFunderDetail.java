package com.formos.huub.domain.response.funder;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class ResponseFunderDetail {

    private UUID id;

    private String name;

    private String funderUrl;

    private String description;

    private String imageUrl;

}
