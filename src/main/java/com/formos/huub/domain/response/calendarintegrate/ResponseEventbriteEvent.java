package com.formos.huub.domain.response.calendarintegrate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ResponseEventbriteEvent {

    private String id;

    private EventName name;

    private String summary;

    private String status;

    private Logo logo;

    @JsonProperty("venue_id")
    private String venueId;

    @JsonProperty("organizer_id")
    private String organizerId;

    private Time start;

    private Time end;

    @JsonProperty("resource_uri")
    private String resourceUri;

    private String url;

    @Getter
    @Setter
    public static class EventName {

        private String text;

        private String html;
    }

    @Getter
    @Setter
    public static class Time {
        private String timezone;

        private String local;

        private Instant utc;
    }

    @Getter
    @Setter
    public static class Logo {
        private String id;
        private Original original;
    }

    @Getter
    @Setter
    public static class Original {
        private String url;
        private Integer width;
        private Integer height;
    }
}
