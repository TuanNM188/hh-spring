package com.formos.huub.domain.response.portals;

import com.formos.huub.domain.enums.PortalFundingStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class SearchPortalFundingResponse {

    private UUID id;

    private String title;

    private BigDecimal amount;

    private Instant publishDate;

    private Instant applicationDeadline;

    private PortalFundingStatusEnum status;
}
