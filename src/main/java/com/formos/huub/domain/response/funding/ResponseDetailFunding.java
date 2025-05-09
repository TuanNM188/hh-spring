package com.formos.huub.domain.response.funding;

import com.formos.huub.domain.enums.PortalFundingStatusEnum;
import com.formos.huub.domain.enums.PortalFundingTypeEnum;
import com.formos.huub.domain.response.categories.ResponseCategories;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseDetailFunding {

    private UUID id;

    private String title;

    private PortalFundingStatusEnum status;

    private Instant dateAdded;

    private Instant publishDate;

    private PortalFundingTypeEnum type;

    private BigDecimal amount;

    private String description;

    private String imageUrl;

    private Boolean hasDeadline;

    private Instant applicationDeadline;

    private String applicationUrl;

    private String applicationProcess;

    private String applicationRequirement;

    private String applicationRestriction;

    private List<String> fundingCategories;

    private ResponseFunder funder;

}
