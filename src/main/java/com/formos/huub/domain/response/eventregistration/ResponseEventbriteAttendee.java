package com.formos.huub.domain.response.eventregistration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseEventbriteAttendee {

    private String id;
    private Instant created;

    @JsonProperty("checked_in")
    private Boolean checkedIn;
    private Boolean cancelled;
    private Boolean refunded;

    private String status;

    @JsonProperty("event_id")
    private String eventId;

    private Profile profile;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Profile {
        private String email;
    }
}
