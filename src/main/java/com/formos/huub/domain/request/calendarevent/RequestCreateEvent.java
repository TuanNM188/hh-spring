package com.formos.huub.domain.request.calendarevent;

import com.formos.huub.domain.enums.EventStatusEnum;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestCreateEvent {

    @RequireCheck
    private String subject;

    @NotNull
    private Instant startTime;

    @NotNull
    private Instant endTime;

    @RequireCheck
    private String location;

    @RequireCheck
    private String summary;

    private String description;

    @RequireCheck
    private String imageUrl;

    @RequireCheck
    private String organizerName;

    @RequireCheck
    private String website;

    @NotNull
    private EventStatusEnum status;

    private List<UUID> portalIds;

    private String timezone;

    private LocalTime startTimeEvent;

    private LocalTime endTimeEvent;

}
