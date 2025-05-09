package com.formos.huub.domain.response.calendarintegrate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseOrganization {

    private String id;
    private String name;
    private String created;
    private String locale;
    private String vertical;
    @JsonProperty("parent_id")
    private String parentId;
    @JsonProperty("image_id")
    private String imageId;

}
