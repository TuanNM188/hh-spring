package com.formos.huub.domain.response.technicaladvisor;

import com.formos.huub.domain.enums.DaysOfWeek;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class ResponseAdvisorAvailability {

    private DaysOfWeek daysOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    private Boolean isActive;
}
