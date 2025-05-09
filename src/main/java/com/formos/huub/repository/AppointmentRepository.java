package com.formos.huub.repository;

import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.AppointmentStatusEnum;
import com.formos.huub.domain.response.appointment.IResponseAppointmentDetail;
import com.formos.huub.domain.response.appointment.IResponseAppointmentReportDetail;
import com.formos.huub.domain.response.appointment.IResponseAppointmentUpcoming;
import com.formos.huub.domain.response.appointment.IResponseHeaderAppointmentDetail;
import com.formos.huub.domain.response.technicalassistance.IResponseAppointmentExport;
import com.formos.huub.domain.response.technicalassistance.IResponseOverviewAppointmentOfTerm;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID>, AppointmentRepositoryCustom {
    @Query(
        value = """
                select
                	p.platform_name as portalName, TO_CHAR(a.created_date at TIME zone 'UTC' at TIME zone :timezone ,'MM/DD/YYYY') as createdDate,
                    u.first_name as firstName, u.last_name as lastName, u.email as email,
                    bn.businessName as businessName, tau.normalized_full_name as advisorName, cp."name" as vendor, c."name" as appointmentCategory,
                    so."name" as appointmentService, a.status as status,
                    TO_CHAR(a.appointment_date at TIME zone 'UTC' at TIME zone :timezone ,'MM/DD/YYYY HH:MI AM') as startDate,
                    TO_CHAR((a.appointment_date + INTERVAL '1 hours') at TIME zone 'UTC' at TIME zone :timezone ,'MM/DD/YYYY HH:MI AM') as endDate,
                    ad.support_description as supportDescription, ad.share_links as shareLinks, soc.serviceOutcomes as serviceOutcomes,
                    arp.report_number as reportNumber, inv.invoice_number as invoiceNumber
                from
                	appointment a
                inner join jhi_user u on a.user_id = u.id
                inner join technical_assistance_submit tas on tas.id = a.technical_assistance_submit_id
                inner join program_term pt on pt.id = tas.program_term_id and pt.is_current is true
                inner join portal p on a.portal_id = p.id
                inner join technical_advisor ta on ta.id = a.technical_advisor_id
                inner join jhi_user tau on tau.id = ta.user_id
                inner join community_partner cp on cp.id = a.community_partner_id
                inner join appointment_detail ad on ad.appointment_id = a.id
                left join appointment_report arp on arp.appointment_id = a.id
                left join invoice inv on a.invoice_id = inv.id
                left join category c on c.id = ad.category_id
                left join service_offered so on so.id = ad.service_id
                left join (
                	select
                		ad2.id as appointmentDetailId,
                		array_agg(so2.name) as serviceOutcomes
                	from appointment_detail ad2
                	left join service_outcome so2 on so2.id::text in (select unnest(string_to_array(ad2.service_outcomes, ',')))
                	group by
                		ad2.id) as soc on
                	soc.appointmentDetailId = ad.id
                left join (
                	select
                		uaf.entry_id as userId,
                		uaf.additional_answer as businessName
                	from
                		user_answer_form uaf join question q on uaf.question_id = q.id
                	where uaf.entry_type = 'USER'
                		and uaf.is_delete is false
                		and q.question_code = 'PORTAL_INTAKE_QUESTION_BUSINESS'
                	group by q.id, uaf.id) as bn on
                	bn.userId = u.id
                where (:portalId is null or p.id = :portalId)
                 and (:startDate is null
                 OR TO_CHAR(a.created_date AT TIME ZONE 'UTC' AT TIME ZONE :timezone, 'YYYY-MM-DD') between :startDate and :endDate)
                order by a.created_date desc
        """,
        nativeQuery = true
    )
    List<IResponseAppointmentExport> getAllByPortalAndDate(UUID portalId, String startDate, String endDate, String timezone);

    @Query(
        "select a from Appointment a " +
        " join a.technicalAdvisor ta " +
        " where (a.status = 'SCHEDULED') " +
        " and a.appointmentDate >= :current and ta.id = :advisorId"
    )
    List<Appointment> getAppointmentAdvisor(Instant current, UUID advisorId);

    @Query(
        value = "select a.* from appointment a where a.appointment_date >= :currentDate and a.technical_advisor_id = :advisorId and a.user_id = :businessOwnerId order by a.appointment_date asc limit 1",
        nativeQuery = true
    )
    Optional<Appointment> getAppointmentByAdvisorAndBusinessOwner(UUID advisorId, UUID businessOwnerId, Instant currentDate);

    @Query(
        "select count(1) > 0 as isExist from Appointment a " +
        " join a.technicalAdvisor ta " +
        " where (a.status = 'SCHEDULED') and a.appointmentDate = :appointmentDate" +
        " and ta.id = :advisorId"
    )
    Boolean isExistAppointmentAdvisor(Instant appointmentDate, UUID advisorId);

    @Query(
        "select count(1) > 0 as isExist from Appointment a " +
        " join a.technicalAdvisor ta" +
        " join a.user u" +
        " where a.status = 'SCHEDULED' and a.appointmentDate = :appointmentDate" +
        " and u.id = :businessOwnerId"
    )
    Boolean isExistAppointmentAdBusinessOwner(Instant appointmentDate, UUID businessOwnerId);

    @Query(
        "select count(1) > 0 from Appointment a join a.technicalAssistanceSubmit tas join a.technicalAdvisor ta" +
        " where a.status != 'CANCELED' and  tas.id = :technicalAssistanceSubmitId " +
        " and (:technicalAdvisorId is null or ta.id = :technicalAdvisorId)"
    )
    Boolean existsByTechnicalAssistanceSubmitId(UUID technicalAssistanceSubmitId, UUID technicalAdvisorId);

    //    @EntityGraph(attributePaths = "appointmentDetail")
    Optional<Appointment> findByIdAndUserId(UUID appointmentId, UUID userId);

    boolean existsById(UUID appointmentId);

    @Query("SELECT count(1) FROM Appointment a " +
        " WHERE a.user.id = :userId AND a.portal.id = :portalId AND a.status != 'CANCELED'"
    )
    Integer countByUserIdAndPortalId(UUID userId, UUID portalId);

    @Query(
        value = """
        select
        	count( CASE WHEN a.status = 'SCHEDULED' THEN 1 END) as numScheduled,
        	count( CASE WHEN a.status = 'OVERDUE' THEN 1  END) as numReportOverdue,
        	count( CASE WHEN a.status = 'INVOICED' or a.status ='COMPLETED' THEN 1 END) as numCompleted,
        	count( CASE WHEN a.status = 'CANCELED' THEN 1  END) as numCanceled
        from
        	portal p
        	inner join "program" pr on pr.portal_id = p.id
        	inner join program_term pt on pr.id = pt.program_id and pt.is_current is true and pt.is_delete is false
        	left join technical_assistance_submit tas ON tas.program_term_id = pt.id and tas.status ='APPROVED'
        	 	 and tas.is_delete is false
        	left join appointment a on a.technical_assistance_submit_id = tas.id and a.is_delete is false
        where (:portalIds is null OR p.id IN (:portalIds))
        and (:communityPartnerId is null OR a.community_partner_id = :communityPartnerId)
        and (:technicalAdvisorId is null OR a.technical_advisor_id = :technicalAdvisorId)
        and (:startDate is null
                 OR TO_CHAR(a.appointment_date AT TIME ZONE 'UTC' AT TIME ZONE :timezone, 'YYYY-MM-DD') between :startDate and :endDate)
        """,
        nativeQuery = true
    )
    IResponseOverviewAppointmentOfTerm getOverviewAppointmentOfTerm(
        List<UUID> portalIds,
        String startDate,
        String endDate,
        UUID communityPartnerId,
        UUID technicalAdvisorId,
        String timezone
    );

    Boolean existsByTechnicalAssistanceSubmit_AssignVendorId(UUID vendorId);

    @Query(
        """
            SELECT a.id as id, u.normalizedFullName as advisorName, ta.id as advisorId, u.imageUrl as advisorImageUrl,
                a.appointmentDate as appointmentDate, a.timezone as timezone,
                bs.linkMeetingPlatform as linkMeetingPlatform, bs.meetingPlatform as meetingPlatform
            FROM Appointment a
            JOIN a.technicalAdvisor ta
            JOIN ta.user u
            JOIN u.bookingSetting bs
            WHERE a.id = :appointmentId
        """
    )
    Optional<IResponseHeaderAppointmentDetail> getHeaderAppointmentDetailById(UUID appointmentId);

    @Query(
        value = """
            SELECT a.id as id, c.id as categoryId, c.name as categoryName, sof.id as serviceId,
                sof.name as serviceName, ad.supportDescription as supportDescription, ad.shareLinks as shareLinks,
                a.appointmentDate as appointmentDate, a.timezone as timezone,
                ad.serviceOutcomes AS selectedServiceOutcomes, a.status as status,
                json_agg(json_build_object('id', sou.id, 'name', sou.name)) AS serviceOutcomes
            FROM Appointment a
            JOIN a.appointmentDetail ad on ad.isDelete = false
            JOIN Category c on c.id = ad.categoryId
            JOIN ServiceOffered sof on sof.id = ad.serviceId
            LEFT JOIN sof.serviceOutcomes sou on sou.isDelete = false
            WHERE a.id = :appointmentId
            group by a.id, ad.id, c.id, sof.id
        """
    )
    Optional<IResponseAppointmentDetail> getAppointmentDetailById(UUID appointmentId);

    List<Appointment> getAllByStatusIn(List<AppointmentStatusEnum> status);

    String queryGetUpcomingAppointment =
        """
            SELECT a.id as id, a.appointment_date as appointmentDate, a.timezone as timezone,
                jsonb_build_object('userId', u.id, 'technicalAdvisorId', ta.id, 'fullName', u.normalized_full_name, 'imageUrl', u.image_url) as technicalAdvisor,
                c.name as category, bs.meeting_platform as meetingPlatform, bs.link_meeting_platform as linkMeetingPlatform,
                CASE WHEN (a.appointment_date - INTERVAL '1 day') >= :currentTime THEN true else false END as isCancelAppointment,
                CASE WHEN (a.appointment_date - INTERVAL '2 hour') >= :currentTime THEN true else false END as isRescheduleAppointment
            FROM appointment a
            JOIN appointment_detail ad on ad.appointment_id = a.id
            JOIN technical_advisor ta on a.technical_advisor_id = ta.id
            JOIN jhi_user u on u.id = ta.user_id
            JOIN booking_setting bs on bs.user_id = u.id
            LEFT JOIN category c on c.id = ad.category_id
            WHERE a.user_id = :userId and a.portal_id = :portalId and a.status = 'SCHEDULED'
                and a.appointment_date >= :currentTime
                and (:ignoreAppointmentId is null or a.id <> :ignoreAppointmentId)
            order by a.appointment_date asc
        """;

    @Query(
        value = queryGetUpcomingAppointment,
        countQuery = "select count(1) from (" + queryGetUpcomingAppointment + ") as temp",
        nativeQuery = true
    )
    Page<IResponseAppointmentUpcoming> findAppointmentUpcomingByUserIdAndPortalId(
        UUID userId,
        UUID portalId,
        UUID ignoreAppointmentId,
        Instant currentTime,
        Pageable pageable
    );

    @Query(
        value = """
            SELECT a.id as id, a.appointment_date as appointmentDate, a.timezone as timezone,
                jsonb_build_object('userId', u.id, 'technicalAdvisorId', ta.id, 'fullName', u.normalized_full_name, 'imageUrl', u.image_url) as technicalAdvisor,
                c.name as category, bs.meeting_platform as meetingPlatform, bs.link_meeting_platform as linkMeetingPlatform,
                CASE WHEN (a.appointment_date - INTERVAL '1 day') >= :currentTime THEN true else false END as isCancelAppointment,
                CASE WHEN (a.appointment_date - INTERVAL '2 hour') >= :currentTime THEN true else false END as isRescheduleAppointment
            FROM appointment a
            JOIN appointment_detail ad on ad.appointment_id = a.id and ad.is_delete = false
            JOIN technical_advisor ta on a.technical_advisor_id = ta.id
            JOIN jhi_user u on u.id = ta.user_id
            JOIN booking_setting bs on bs.user_id = u.id
            LEFT JOIN category c on c.id = ad.category_id
            WHERE a.user_id = :userId and a.portal_id = :portalId and a.status = 'SCHEDULED'
                and a.appointment_date >= :currentTime
            order by a.appointment_date asc
             limit 1
        """,
        nativeQuery = true
    )
    IResponseAppointmentUpcoming findFirstAppointmentUpcomingByUserIdAndPortalId(UUID userId, UUID portalId, Instant currentTime);

    @Query(
        """
            SELECT ROUND(AVG(ad.rating), 2)
            FROM Appointment a
            JOIN a.appointmentDetail ad
            WHERE (:portalId is null or a.portal.id = :portalId)
                AND ad.rating is not null and ad.rating > 0
        """
    )
    Optional<Float> getAvgRatingByPortal(UUID portalId);

    @Query(
        """
            SELECT ROUND(AVG(ad.rating), 2)
            FROM Appointment a
            JOIN a.appointmentDetail ad
            WHERE a.communityPartner.id = :communityPartnerId
                AND ad.rating is not null and ad.rating > 0
        """
    )
    Optional<Float> getAvgRatingByCommunityPartnerId(UUID communityPartnerId);

    @Query("SELECT a FROM Appointment a where a.status = :status AND a.appointmentDate > :start AND a.appointmentDate <= :end")
    List<Appointment> findByStatusAndAppointmentDateBefore(AppointmentStatusEnum status, Instant start, Instant end);

    /**
     * Find appointments by technical advisor, portal, status and appointment date range
     */
    List<Appointment> findByTechnicalAdvisorAndPortalAndStatusAndAppointmentDateBetween(
        TechnicalAdvisor technicalAdvisor,
        Portal portal,
        AppointmentStatusEnum status,
        Instant startDate,
        Instant endDate
    );

    /**
     * Find appointments by status and appointment date range
     */
    List<Appointment> findByStatusAndAppointmentDateBetween(AppointmentStatusEnum status, Instant startDate, Instant endDate);

    @Query(
        value = """
            SELECT ar.businessOwnerAttended AS businessOwnerAttended, ar.description AS description, ar.feedback AS feedback,
                ar.serviceOutcomes AS selectedServiceOutcomes,
                COALESCE(json_agg(json_build_object('id', ea.id, 'name', ea.name, 'path', ea.path, 'type', ea.type, 'suffix', ea.suffix, 'icon', ea.icon)) FILTER (WHERE ea.id IS NOT NULL), '[]') AS attachments
            FROM Appointment a
            JOIN a.appointmentReport ar
            LEFT JOIN EntityAttachment ea ON ar.id = ea.entityId AND ea.entityType = 'APPOINTMENT_REPORT'
            WHERE a.id = :appointmentId
            GROUP BY a.id, ar.id
        """
    )
    Optional<IResponseAppointmentReportDetail> getAppointmentReportDetailById(UUID appointmentId);

    @Query("select count(1) > 0 as isExist from Appointment a join a.technicalAdvisor ta " +
        " join a.technicalAssistanceSubmit tas " +
        " join tas.programTerm pt" +
        " where pt.isCurrent is true " +
        " and a.status != 'CANCELED' and a.status != 'INVOICED' " +
        " and ta.id = :technicalAdvisorId ")
    boolean existsUseByTechnicalAdvisor(UUID technicalAdvisorId);
}
