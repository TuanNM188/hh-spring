package com.formos.huub.domain.response.calendarintegrate;

import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseCalendarEvent implements Serializable {

    private String id;

    private String externalEventId;

    private String externalCalendarId;

    private String subject;

    private String body;

    private Instant startTime;

    private Instant endTime;

    private String timezone;

    private String location;

    private String summary;

    private String description;

    private String eventbriteStatus;

    private String imageUrl;

    private String eventUrl;

    private Boolean isAllDay;

    private  String organizerName;


}
