package com.formos.huub.framework.utils.model;

import java.time.Instant;
import lombok.Data;

@Data
public class DateRange {

    private Instant startDate;
    private Instant endDate;

    public DateRange(Instant startDate, Instant endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
