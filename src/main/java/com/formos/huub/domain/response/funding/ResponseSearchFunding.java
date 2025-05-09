package com.formos.huub.domain.response.funding;

import com.formos.huub.domain.enums.PortalFundingStatusEnum;
import com.formos.huub.domain.enums.PortalFundingTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseSearchFunding {

    private UUID id;

    private String title;

    private String description;

    private BigDecimal amount;

    private Instant dateAdded;

    private PortalFundingTypeEnum type;

    private String imageUrl;

    private Instant applicationDeadline;

    private List<String> fundingCategories;

    private Boolean isFavorite;

    private Boolean isSubmitted;

    private PortalFundingStatusEnum status;

    private String applicationUrl;
}
