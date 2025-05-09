package com.formos.huub.domain.response.calendarevent;

import com.formos.huub.domain.enums.EventStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseCalendarEventDetail {

    private UUID id;

    private String subject;

    private Instant startTime;

    private Instant endTime;

    private String location;

    private String summary;

    private String description;

    private String imageUrl;

    private String organizerName;

    private String website;

    private EventStatusEnum status;

    private List<UUID> portalIds;
}
