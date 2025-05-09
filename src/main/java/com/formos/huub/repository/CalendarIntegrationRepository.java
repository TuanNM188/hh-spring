package com.formos.huub.repository;

import com.formos.huub.domain.entity.CalendarIntegration;
import com.formos.huub.domain.enums.CalendarStatusEnum;
import com.formos.huub.domain.enums.CalendarTypeEnum;
import com.formos.huub.domain.enums.IntegrateByEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CalendarIntegrationRepository extends JpaRepository<CalendarIntegration, UUID> {

    Optional<CalendarIntegration> findByBookingSettingUserId(UUID userId);

    Optional<CalendarIntegration> findByBookingSettingId(UUID bookingSettingId);

    List<CalendarIntegration> findAllByPortalIdAndIntegrateBy(UUID portalId, IntegrateByEnum integrateBy);

    @Query("select ci from CalendarIntegration ci where ci.portal.id is null and ci.calendarStatus = :calendarStatus and ci.calendarType IN (:calendarTypes) ")
    List<CalendarIntegration> findAllByCalendarStatusAndCalendarTypeIn(CalendarStatusEnum calendarStatus, List<CalendarTypeEnum>  calendarTypes);

    @Query("select ci from CalendarIntegration ci where ci.portal.id is not null and ci.calendarStatus = :calendarStatus and ci.calendarType IN (:calendarTypes) " +
        "AND ci.integrateBy = :integrateBy AND (ci.syncEventStatus IS NULL OR ci.syncEventStatus != 'ERROR')")
    List<CalendarIntegration> findAllByCalendarStatusAndCalendarTypeInAndPortal(CalendarStatusEnum calendarStatus, List<CalendarTypeEnum> calendarTypes, IntegrateByEnum integrateBy);

    Optional<CalendarIntegration> findByCalendarId(String calendarId);

    Optional<CalendarIntegration> findByUrlAndPortalId(String url, UUID portalId);

    @Query("SELECT ci FROM CalendarIntegration ci WHERE ci.calendarId LIKE CONCAT(:calendarId, '%')")
    List<CalendarIntegration> findAllByCalendarId(String calendarId);
}
