package com.formos.huub.domain.request.portals;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestProgramTerm {

    private UUID id;

    @NotNull
    private UUID programManagerId;

    @RequireCheck
    private String name;

    @NotNull
    private Instant startDate;

    @NotNull
    private Instant endDate;

    @NotNull
    private BigDecimal budget;

    private @Valid List<RequestProgramTermVendor> vendorRequests;
}
