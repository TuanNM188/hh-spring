package com.formos.huub.domain.response.bookingsetting;

import com.formos.huub.domain.enums.DaysOfWeek;
import java.time.LocalTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseAvailability {

    private UUID id;

    private DaysOfWeek daysOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    private Boolean isActive;
}
