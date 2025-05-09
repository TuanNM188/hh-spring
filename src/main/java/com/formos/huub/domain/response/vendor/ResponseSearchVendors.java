package com.formos.huub.domain.response.vendor;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ResponseSearchVendors {

    private UUID id;

    private UUID communityPartnerId;

    private String communityPartnerName;

    private UUID navigatorId;

    private String navigatorName;

    private Integer totalAssigns;

    private BigDecimal budgetAssign;

    private BigDecimal budgetInProgress;

    private BigDecimal budgetCompleted;
}
