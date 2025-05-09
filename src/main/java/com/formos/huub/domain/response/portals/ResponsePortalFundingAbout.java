package com.formos.huub.domain.response.portals;

import com.formos.huub.domain.enums.PortalFundingStatusEnum;
import com.formos.huub.domain.enums.PortalFundingTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class ResponsePortalFundingAbout {

    private String title;

    private PortalFundingStatusEnum status;

    private Instant dateAdded;

    private Instant publishDate;

    private PortalFundingTypeEnum type;

    private BigDecimal amount;

    private String description;

    private String imageUrl;

    private List<String> fundingCategories;

    private List<UUID> portalIds;

}
