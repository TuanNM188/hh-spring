package com.formos.huub.domain.response.portals;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
public class ResponseProgramTermDateRange {

    private Instant startDate;

    private Instant endDate;

}
