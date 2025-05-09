package com.formos.huub.domain.response.technicalassistance;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseTechnicalAssistanceOverview {

    private Integer  numSubmitted;

    private Integer  numVendorAssigned;

    private Integer  numActive;

    private Integer  numDenied;

    private Integer  numExpired;
}
