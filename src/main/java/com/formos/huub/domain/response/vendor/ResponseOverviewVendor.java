package com.formos.huub.domain.response.vendor;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ResponseOverviewVendor {
    private BigDecimal available;

    private BigDecimal assigned;

    private BigDecimal inProgress;

    private BigDecimal complete;

}
