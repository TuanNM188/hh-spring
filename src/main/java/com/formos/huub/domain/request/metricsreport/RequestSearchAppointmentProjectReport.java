package com.formos.huub.domain.request.metricsreport;

import com.formos.huub.domain.request.common.SearchConditionRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestSearchAppointmentProjectReport extends SearchConditionRequest {

    private UUID portalId;

    private List<UUID> portalIds;

    private String startDate;

    private String endDate;

    private String searchKeyword;

}
