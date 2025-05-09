package com.formos.huub.domain.response.calendarintegrate;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ResponseOutlookListEvent {
    private List<ResponseOutlookEvents> value = new ArrayList<>();

    @SerializedName("@odata.nextLink")
    private String nextLink;
}
