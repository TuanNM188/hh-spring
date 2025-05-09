package com.formos.huub.domain.response.metricsreport;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class ResponseSearchAppointmentProjectReport {

    private UUID id;

    private Instant reportSubmissionDate;

    private String invoiceNumber;

    private String businessOwnerName;

    private String advisorName;

    private Float hours;

    private String pdfUrl;

    private String pdfFilename;
}
