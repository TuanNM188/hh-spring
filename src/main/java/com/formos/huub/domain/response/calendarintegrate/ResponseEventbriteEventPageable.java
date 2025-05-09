package com.formos.huub.domain.response.calendarintegrate;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseEventbriteEventPageable extends AbstractResponsePageable<ResponseEventbriteEvent> {

    private List<ResponseEventbriteEvent> events;

    @Override
    public List<ResponseEventbriteEvent> getItems() {
        return events;
    }
}
