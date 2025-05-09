package com.formos.huub.repository;

import com.formos.huub.domain.entity.CalendarToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CalendarTokenRepository extends JpaRepository<CalendarToken, UUID> {

    Optional<CalendarToken> findByCalendarIntegrationId(UUID calendarId);

    void deleteByCalendarIntegrationId(UUID calendarIntegrateId);
}
