package com.formos.huub.domain.response.metricsreport;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ResponseInvoicedAmountByAdvisor {

    private UUID id;

    private Instant invoiceDate;

    private UUID advisorId;

    private String advisorName;

    private Float appointmentHours;

    private Float projectHours;

    private BigDecimal totalAmount;

    private String filePath;

    private String fileName;
}
