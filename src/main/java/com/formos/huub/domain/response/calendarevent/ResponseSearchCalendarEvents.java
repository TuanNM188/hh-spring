package com.formos.huub.domain.response.calendarevent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.formos.huub.domain.enums.EventStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ResponseSearchCalendarEvents {

    private UUID id;

    @JsonProperty("title")
    private String subject;

    @JsonProperty("start")
    private Instant startTime;

    private EventStatusEnum status;

    private String createdBy;

    private String description;

    @JsonProperty("end")
    private Instant endTime;

    private String imageUrl;

    private String website;

    private String initBy;

}
