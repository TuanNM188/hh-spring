package com.formos.huub.domain.response.language;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseLanguage {
    private UUID id;

    private String name;

    private String code;
}
