package com.formos.huub.domain.response.businessowner;

import java.io.Serializable;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseSearchEventRegistrations implements Serializable {

    private UUID calendarEventId;
    private String eventName;
    private String eventDate;
}
