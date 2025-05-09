package com.formos.huub.repository;

import com.formos.huub.domain.entity.EventRegistration;
import com.formos.huub.domain.response.calendarevent.IResponseEventRegistrationExport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, UUID>, EventRegistrationRepositoryCustom {

    @Query(value = """
        select p.platform_name as portalName,
        TO_CHAR(er.created_date at TIME zone 'UTC' at TIME zone :timezone ,'MM/DD/YYYY') as createdDate ,
        u.first_name as firstName, u.last_name as lastName,
        u.email  as email, bn.businessName, ce.subject , ce.description ,
        TO_CHAR(ce.start_time at TIME zone 'UTC' at TIME zone :timezone ,'MM/DD/YYYY HH:MI AM') as startTime
        from event_registrations er
        inner join calendar_event ce on er.calendar_event_id = ce.id and ce.is_delete is false and er.is_delete  is false
        inner join business_owner bo on bo.id = er.business_owner_id
        inner join jhi_user u on u.id = bo.user_id
        left join portal_calendar_event pce on ce.id= pce.calendar_event_id
        left join portal p on p.id = pce.portal_id
        left join (
            select
                uaf.entry_id as userId,
                uaf.additional_answer as businessName
            from
                user_answer_form uaf join question q on uaf.question_id = q.id
            where uaf.entry_type = 'USER'
                and uaf.is_delete is false
                and q.question_code = 'PORTAL_INTAKE_QUESTION_BUSINESS'
            group by q.id, uaf.id) as bn on bn.userId = u.id
         where (:portalId is null or p.id = :portalId)
         and (:startDate is null
         OR TO_CHAR(er.created_date AT TIME ZONE 'UTC' AT TIME ZONE :timezone, 'YYYY-MM-DD') between :startDate and :endDate)
         order by er.created_date desc
        """, nativeQuery = true)
    List<IResponseEventRegistrationExport> getAllByPortalAndDate(UUID portalId, String startDate, String endDate, String timezone);

    Optional<EventRegistration> findByExternalAttendeeId(String externalAttendeeId);

    List<EventRegistration> findByExternalAttendeeIdIn(List<String> externalAttendeeId);

}
