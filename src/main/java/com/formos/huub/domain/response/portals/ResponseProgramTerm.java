package com.formos.huub.domain.response.portals;

import com.formos.huub.domain.enums.StatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseProgramTerm {

    private UUID id;

    private String name;

    private String programManagerId;

    private Instant startDate;

    private Instant endDate;

    private BigDecimal budget;

    private StatusEnum status;

    private Instant createdDate;

    private List<ResponseProgramTermVendor> programTermVendors;
}
