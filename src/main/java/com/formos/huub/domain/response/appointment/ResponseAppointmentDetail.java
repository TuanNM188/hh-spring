package com.formos.huub.domain.response.appointment;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseAppointmentDetail {

    private UUID id;

    private UUID categoryId;

    private UUID serviceId;

    private String supportDescription;

    private String shareLinks;

    private String serviceOutcomes;

    private Integer rating;

    private String feedback;

    private String comments;

    private Long useAwardHours;
}
