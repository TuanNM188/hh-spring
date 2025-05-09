package com.formos.huub.domain.response.technicalassistance;

import com.formos.huub.domain.enums.ApprovalStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ResponseSearchApplication {

    private UUID id;

    private UUID projectId;

    private UUID userId;

    private String businessOwnerName;

    private Instant lastAppointment;

    private Instant upcomingAppointment;

    private String projectStatus;

    private ApprovalStatusEnum applicationStatus;

    private Float remainingAwardHours;

    private String platformName;
}
