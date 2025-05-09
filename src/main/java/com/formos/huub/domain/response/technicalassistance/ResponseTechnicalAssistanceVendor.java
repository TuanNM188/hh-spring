package com.formos.huub.domain.response.technicalassistance;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseTechnicalAssistanceVendor {

    private UUID id;

    private String vendorName;

    private Integer technicalAssistanceAssignment;

    private Long budgetAssignment;

    private Long budgetCompleted;

    private Long budgetUtilized;
}
