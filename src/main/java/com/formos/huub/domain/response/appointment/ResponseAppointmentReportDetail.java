package com.formos.huub.domain.response.appointment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseAppointmentReportDetail {

    private Boolean businessOwnerAttended;

    private String description;

    private String feedback;

    private String selectedServiceOutcomes;

    private String attachments;

}
