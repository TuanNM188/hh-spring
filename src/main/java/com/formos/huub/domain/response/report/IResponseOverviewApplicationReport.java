package com.formos.huub.domain.response.report;

import java.math.BigDecimal;


public interface IResponseOverviewApplicationReport {


     BigDecimal getMonthlyExpense();

    BigDecimal getAdvisorRatingAverage();

     Float getTotalHours();

     Integer getNumberOfAdvisor();
}
