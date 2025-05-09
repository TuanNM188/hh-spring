package com.formos.huub.domain.request.bookingsetting;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.formos.huub.domain.enums.DaysOfWeek;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
public class RequestTechnicalAdvisorAvailability {

    private DaysOfWeek daysOfWeek;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime endTime;

    private Boolean isActive;
}
