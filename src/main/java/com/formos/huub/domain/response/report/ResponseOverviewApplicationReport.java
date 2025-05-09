package com.formos.huub.domain.response.report;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class ResponseOverviewApplicationReport {

    private BigDecimal monthlyExpense;

    private BigDecimal advisorRatingAverage;

    private Float totalHours;

    private Integer numberOfAdvisor;

    private Instant startDate;

    private Instant endDate;
}
