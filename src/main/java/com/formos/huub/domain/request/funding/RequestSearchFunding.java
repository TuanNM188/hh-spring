package com.formos.huub.domain.request.funding;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestSearchFunding {

    private UUID userId;

    private UUID portalId;

    private String excludeIds;

    private String searchKeyword;

    private Long amountMin;

    private Long amountMax;

    private String fundingTypes;

    private String fundingCategories;

    private String status;

    private String filterStatus;

    private Boolean isFavorite;

    private String fundingPage;

    private String currentDate;

    private String endExpiringSoonDate;

    private String startRecentlyAdded;
}
