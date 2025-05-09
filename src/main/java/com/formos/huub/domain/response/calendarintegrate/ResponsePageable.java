package com.formos.huub.domain.response.calendarintegrate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
public class ResponsePageable {

    @JsonProperty("object_count")
    private int objectCount;
    private String continuation;
    @JsonProperty("page_count")
    private int pageCount;
    @JsonProperty("page_size")
    private int pageSize;
    @JsonProperty("has_more_items")
    private boolean hasMoreItems;
    @JsonProperty("page_number")
    private int pageNumber;

}
