package com.formos.huub.domain.response.calendarevent;

import com.formos.huub.domain.request.common.SearchConditionRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestSearchCalendarEvents extends SearchConditionRequest {
    private UUID portalId;

    private String searchKeyword;

    private String startDate;

    private String endDate;

    private String searchMonth;

    private String status;

    private String organizerName;

    private String regexHtml;

    private String currentDate;
}
