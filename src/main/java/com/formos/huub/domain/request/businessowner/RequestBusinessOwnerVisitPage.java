package com.formos.huub.domain.request.businessowner;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestBusinessOwnerVisitPage {

    @NotNull
    private String fieldName;

}
