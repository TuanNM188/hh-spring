package com.formos.huub.domain.request.portals;

import com.formos.huub.domain.enums.StatusEnum;
import com.formos.huub.framework.validation.constraints.NumericCheck;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class RequestProgramTermVendor {

    private UUID id;

    private UUID vendorId;

    @NotNull
    private BigDecimal contractedRate;

    @NotNull
    private BigDecimal negotiatedRate;

    private BigDecimal vendorBudget;

    private Float calculatedHours;

    private StatusEnum status;

}
