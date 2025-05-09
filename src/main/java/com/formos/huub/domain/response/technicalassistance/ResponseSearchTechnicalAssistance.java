package com.formos.huub.domain.response.technicalassistance;

import com.formos.huub.domain.enums.ApprovalStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ResponseSearchTechnicalAssistance {

    private UUID id;

    private ApprovalStatusEnum status;

    private Instant submitAt;

    private String fullName;

    private String vendorName;

}
