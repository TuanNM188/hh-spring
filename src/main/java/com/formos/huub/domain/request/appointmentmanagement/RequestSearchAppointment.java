package com.formos.huub.domain.request.appointmentmanagement;

import com.formos.huub.domain.enums.AppointmentStatusEnum;
import com.formos.huub.domain.request.common.SearchConditionRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestSearchAppointment extends SearchConditionRequest {

    private UUID portalId;

    private List<UUID> portalIds;

    private UUID technicalAssistanceId;

    private UUID communityPartnerId;

    private UUID technicalAdvisorId;

    private String startDate;

    private String endDate;

    private String status;

    private List<String> appointmentStatus;

    private String searchKeyword;

    private Boolean isCurrent;
}
