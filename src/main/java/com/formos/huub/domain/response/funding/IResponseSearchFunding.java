package com.formos.huub.domain.response.funding;

import com.formos.huub.domain.enums.PortalFundingStatusEnum;
import com.formos.huub.domain.enums.PortalFundingTypeEnum;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface IResponseSearchFunding {

    UUID getId();

    String getTitle();

    String getDescription();

    BigDecimal getAmount();

    Instant getDateAdded();

    PortalFundingTypeEnum getType();

    String getImageUrl();

    Instant getApplicationDeadline();

    String getFundingCategories();

    Boolean getIsFavorite();

    Boolean getIsSubmitted();

    PortalFundingStatusEnum getStatus();

    String getApplicationUrl();

}
