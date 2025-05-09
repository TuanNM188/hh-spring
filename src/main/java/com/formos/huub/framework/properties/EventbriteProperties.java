package com.formos.huub.framework.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "eventbrite")
public class EventbriteProperties {

    private String singleEventUrl;
    private String eventAttendeesUrl;
    private String attendeeUrl;

    private String organizerEventsUrl;

    private String venueDetailUrl;

    private String organizerUrl;

    private String apiToken;
    private String organizationsUrl;
    private String organizationEventsUrl;
}
