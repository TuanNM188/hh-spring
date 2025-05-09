package com.formos.huub.domain.response.technicalassistance;

import com.formos.huub.domain.enums.ApprovalStatusEnum;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseInfoTechnicalAssistance {

    private UUID technicalAssistanceId;

    private ApprovalStatusEnum applicationStatus;

    private Float assignAwardHours;

    private Float remainingAwardHours;

    private String deniedReason;

    private List<ResponseInfoAdvisor> advisors;

    private ResponseInfoVendor vendor;
}
