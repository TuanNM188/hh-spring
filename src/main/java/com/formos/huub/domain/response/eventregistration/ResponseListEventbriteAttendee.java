package com.formos.huub.domain.response.eventregistration;

import com.formos.huub.domain.response.calendarintegrate.AbstractResponsePageable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseListEventbriteAttendee extends AbstractResponsePageable<ResponseEventbriteAttendee> {

    private List<ResponseEventbriteAttendee> attendees;

    @Override
    public List<ResponseEventbriteAttendee> getItems() {
        return attendees;
    }
}
