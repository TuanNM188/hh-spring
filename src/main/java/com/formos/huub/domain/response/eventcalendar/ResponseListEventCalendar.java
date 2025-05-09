package com.formos.huub.domain.response.eventcalendar;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.formos.huub.domain.enums.EventStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ResponseListEventCalendar {

    private UUID id;

    @JsonProperty("title")
    private String subject;

    private String body;

    @JsonProperty("start")
    private Instant startTime;

    @JsonProperty("end")

    private Instant endTime;

    private String location;

    private String summary;

    private String description;

    private String imageUrl;

    private String website;

    private String initBy;

    private EventStatusEnum status;

}
