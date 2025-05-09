package com.formos.huub.domain.request.learninglibrary;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestSectionFile {

    private String name;

    private String description;

    private String mediaUrl;

    private Integer position;
}
