package com.formos.huub.domain.response.calendarintegrate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseOrganizer {

    private String id;
    private String name;
    private String url;
    private String website;
    private String facebook;

    @JsonProperty("organization_id")
    private String organizationId;


}
