package com.formos.huub.domain.response.portals;

import com.formos.huub.domain.enums.StatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ResponseProgramTermVendor {

    private UUID id;

    private UUID vendorId;

    private BigDecimal contractedRate;

    private BigDecimal negotiatedRate;

    private BigDecimal vendorBudget;

    private Float calculatedHours;

    private StatusEnum status;

    private Instant createdDate;

}
