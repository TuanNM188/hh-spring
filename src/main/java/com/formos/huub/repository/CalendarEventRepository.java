package com.formos.huub.repository;

import com.formos.huub.domain.entity.CalendarEvent;
import com.formos.huub.domain.entity.LearningLibrary;
import com.formos.huub.domain.enums.EventStatusEnum;
import com.formos.huub.domain.request.eventcalendar.RequestSearchEvents;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, UUID>, CalendarEventRepositoryCustom {


    @Query(value = "select distinct ce.* from calendar_event ce " +
        " left join portal_calendar_event pce on pce.calendar_event_id = ce.id " +
        " where ce.is_delete is false  and ce.status = 'PUBLISHED'" +
        " And (coalesce(:#{#cond.searchKeyword}, null) is null or lower(ce.subject) like concat('%', lower(:#{#cond.searchKeyword}) ,'%'))" +
        " And (coalesce(:#{#cond.startDate}, null) is null or TO_CHAR(ce.start_time AT TIME ZONE 'UTC' AT TIME ZONE :timezone, 'YYYY-MM-DD') between :#{#cond.startDate} and :#{#cond.endDate})" +
        " And (coalesce(:#{#cond.portalId}, null) is null or pce.portal_id = :#{#cond.portalId})" +
        " And (coalesce(:#{#cond.status}, null) is null or ce.status = :#{#cond.status})", nativeQuery = true)
    List<CalendarEvent> searchCalendarEventListView(@Param("cond") RequestSearchEvents request, String timezone);

    @Query(value = "select distinct ce.* from calendar_event ce " +
        "        where ce.is_delete is false  and ce.status = 'PUBLISHED'" +
        "        And lower(ce.organizer_name) like lower(:organizerName) and ce.id != :excludeId" +
        "        order by ce.start_time desc limit 3", nativeQuery = true)
    List<CalendarEvent> getEventRelatedByEvent(UUID excludeId, String organizerName);

    void deleteAllByCalendarIntegrationId(UUID calendarIntegrateId);

    void deleteAllByExternalEventIdIn(List<String> externalEventIds);

    List<CalendarEvent> findAllByExternalEventIdIn(List<String> externalEventIds);

    @Query("""
        SELECT ce
        FROM CalendarEvent ce
        WHERE ce.externalEventId in :externalEventId and ce.status <> :status
    """)
    List<CalendarEvent> findAllByExternalEventIdInAndStatusNot(List<String> externalEventId, EventStatusEnum status);

    @Query("""
        SELECT ce
        FROM CalendarEvent ce
        WHERE ce.isHuubEvent = true
    """)
    List<CalendarEvent> findAllHuubEvent();

    void deleteAllByExternalCalendarIdIn(List<String> externalCalendarIds);

    @Query(
        "select ce  from CalendarEvent ce left join ce.calendarIntegration ci where ci.id = :calendarIntegrateId OR ce.externalEventId IN (:externalEventIds)"
    )
    List<CalendarEvent> getAllByCalendarIntegrationIdOrExternalIds(UUID calendarIntegrateId, List<String> externalEventIds);

    List<CalendarEvent> getAllByCalendarIntegrationId(UUID calendarIntegrateId);

    List<CalendarEvent> getAllByCalendarIntegrationIdIn(List<UUID> calendarIntegrateIds);

    @Modifying
    @Query("UPDATE CalendarEvent ce SET ce.status = :status WHERE ce.externalEventId IN :externalIds")
    void updateStatusForDeletedEvents(Set<String> externalIds, EventStatusEnum status);

    boolean existsByIdAndCreatedBy(UUID id, String createdBy);


    @Query(value = "select ce.* from calendar_event ce join portal_calendar_event pce" +
        " on pce.calendar_event_id = ce.id and ce.is_delete is false " +
        " where " +
        " ce.status = 'PUBLISHED' and pce.portal_id = :portalId " +
        " and ce.start_time >= :time " +
        " ORDER BY  DATE(ce.created_date) desc, ce.start_time asc ", nativeQuery = true)
    List<CalendarEvent> findLatestByPortal(UUID portalId, Instant time, Pageable pageable);

    List<CalendarEvent> findAllByIsHuubEventAndStatusNot(boolean huubEvent, EventStatusEnum status);
}
