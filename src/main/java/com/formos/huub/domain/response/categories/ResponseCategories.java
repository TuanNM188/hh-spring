package com.formos.huub.domain.response.categories;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseCategories {

    private UUID id;

    private String name;

    private String description;

    private String iconUrl;

    private String serviceOptions;
}
