package com.formos.huub.domain.request.outsidereport;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestSearchOutsideReport {
    private UUID portalId;
    private Integer year;
}
