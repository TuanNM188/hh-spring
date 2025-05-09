package com.formos.huub.repository;

import com.formos.huub.domain.request.businessowner.RequestSearchEventRegistrations;
import com.formos.huub.domain.response.businessowner.ResponseSearchEventRegistrations;
import com.formos.huub.domain.response.calendarevent.RequestSearchCalendarEvents;
import com.formos.huub.domain.response.calendarevent.ResponseSearchCalendarEvents;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CalendarEventRepositoryCustom {
    Page<ResponseSearchCalendarEvents> searchByTermAndCondition(RequestSearchCalendarEvents condition, Pageable pageable);

    Page<ResponseSearchEventRegistrations> searchEventRegistrations(
        UUID bussinessOwnerId,
        RequestSearchEventRegistrations request,
        Pageable pageable
    );
}
