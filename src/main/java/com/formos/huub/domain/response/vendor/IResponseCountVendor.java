package com.formos.huub.domain.response.vendor;

import java.math.BigDecimal;

public interface IResponseCountVendor {

    BigDecimal getTotalBudget();

    BigDecimal getAssigned();

    BigDecimal getInProgress();

    BigDecimal getComplete();

}
