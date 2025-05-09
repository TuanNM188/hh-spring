package com.formos.huub.repository;

import com.formos.huub.domain.entity.Portal;
import com.formos.huub.domain.entity.TechnicalAdvisor;
import com.formos.huub.domain.enums.EntryTypeEnum;
import com.formos.huub.domain.enums.FormCodeEnum;
import com.formos.huub.domain.enums.UserStatusEnum;
import com.formos.huub.domain.request.technicaladvisor.RequestSearchAdvisor;
import com.formos.huub.domain.response.answerform.IResponseProfileCompletionStatus;
import com.formos.huub.domain.response.communitypartner.IResponseAdvisorByAppointment;
import com.formos.huub.domain.response.communitypartner.IResponseAssignAdvisor;
import com.formos.huub.domain.response.communitypartner.IResponseTechnicalAdvisor;
import com.formos.huub.domain.response.technicaladvisor.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TechnicalAdvisorRepository extends JpaRepository<TechnicalAdvisor, UUID>, TechnicalAdvisorRepositoryCustom {
    String QUERY_SEARCH_BROWSE_ADVISORS =
        """
        select u.id as userId, u.normalized_full_name as fullName, ta.id as technicalAdvisorId, u.image_url as imageUrl, u.bio as bio, u.personal_website as personalWebsite,
                	lg.languages as languages, c.categories as categories
                from
                	technical_advisor ta join jhi_user u on u.id = ta.user_id and ta.is_delete is false
                join booking_setting b on b.user_id = u.id and b.is_delete is false
                join specialty_user su on su.user_id = u.id and su.is_delete is false
                join advisement_category ac on ac.user_id = u.id and ac.is_delete is false
                left join community_partner cp on ta.community_partner_id = cp.id and cp.status ='ACTIVE'
                left join portal_community_partner pcp ON pcp.community_partner_id  = cp.id
                left join (
                	select
                		uaf.entry_id as userId,
                		uaf.entry_type,
                		q.question_code,
                		array_agg(distinct l.id) as languageIds,
                		array_agg(distinct l.name) as languageNames,
                		json_agg(json_build_object('id',l.id,'name',l.name))::TEXT as languages
                	from user_answer_form uaf
                	join question q on uaf.question_id = q.id
                	left join user_answer_option uao on uaf.id = uao.user_answer_form_id
                	left join "language" l on uao.option_id = l.id and q.option_type = 'LANGUAGE'
                	where
                		uaf.entry_type = 'USER'
                		and uaf.is_delete is false
                		and q.question_code = 'QUESTION_LANGUAGE_SPOKEN'
                	group by q.id, uaf.id) as lg on lg.userId = u.id
                left join (
                	select ac.user_id,
                		jsonb_agg(distinct jsonb_build_object('id',c.id,'name',c.name))::TEXT as categories,
                		array_agg(distinct c.id) as categoriesId,
                        array_agg(distinct c.name) as categoriesName,
                		array_agg(distinct so.name) as serviceNames,
                		array_agg(distinct so.id) as services
                	from advisement_category ac
                	left join category c on ac.category_id = c.id and ac.is_delete is false and c.is_delete is false
                	left join service_offered so on ac.service_id = so.id and so.is_delete is false
                	group by ac.user_id) as c on
                	c.user_id = u.id
                left join (
                	select su.user_id,
                	array_agg(distinct s.id) as specialties,
                	array_agg(distinct sa.id) as areas,
                	array_agg(distinct s.name) as specialtyNames,
                	array_agg(distinct sa.name) as areaNames
                	from specialty_user su
                	left join specialty s on su.specialty_id = s.id and su.is_delete is false and s.is_delete is false
                	left join specialty_area sa on su.specialty_area_id = sa.id and sa.is_delete is false
                	group by su.user_id) as s on
                	s.user_id = u.id
                left join user_answer_form uafDemographics
                     on uafDemographics.entry_id = u.id
                     and uafDemographics.entry_type = 'USER'
                left join question qa
                     on uafDemographics.question_id = qa.id and qa.form_code = 'DEMOGRAPHICS'
        where
        	u.status = 'ACTIVE'
        	and uafDemographics.id IS NOT NULL
        	and (:communityPartnerId is null or ta.community_partner_id = :communityPartnerId)
        	and (coalesce(:#{#cond.technicalAssistanceId}, null) is null or ta.id in (select taa.technical_advisor_id
                 from technical_assistance_advisor taa join technical_assistance_submit tas on taa.technical_assistance_submit_id = tas.id
                 where tas.id = :#{#cond.technicalAssistanceId}
            ))
        	and (coalesce(:#{#cond.portalId}, null) is null OR pcp.portal_id = :#{#cond.portalId} )
        	and (coalesce(:#{#cond.searchKeyword}, null) is null
        	    or u.normalized_full_name ilike concat('%', lower(:#{#cond.searchKeyword}) ,'%')
        	    or lg.languageNames::text ilike concat('%', lower(:#{#cond.searchKeyword}) ,'%')
        	    or c.categoriesName::text ilike concat('%', lower(:#{#cond.searchKeyword}) ,'%')
        	    or c.serviceNames::text ilike concat('%', lower(:#{#cond.searchKeyword}) ,'%')
        	    or s.specialtyNames::text ilike concat('%', lower(:#{#cond.searchKeyword}) ,'%')
        	    or s.areaNames::text ilike concat('%', lower(:#{#cond.searchKeyword}) ,'%'))
        	and (coalesce(:#{#cond.communityPartnerIds}, null) is null OR ta.community_partner_id::text = ANY(string_to_array(:#{#cond.communityPartnerIds}, ',')))
        	and (coalesce(:#{#cond.categories}, null) is null OR c.categoriesId::text[] &&  string_to_array(:#{#cond.categories} , ','))
        	and (coalesce(:#{#cond.services}, null) is null OR c.services::text[] &&  string_to_array(:#{#cond.services} , ','))
        	and (coalesce(:#{#cond.specialties}, null) is null OR s.specialties::text[] &&  string_to_array(:#{#cond.specialties} , ','))
        	and (coalesce(:#{#cond.areas}, null) is null OR s.areas::text[] &&  string_to_array(:#{#cond.areas} , ','))
        	and (coalesce(:#{#cond.languages}, null) is null OR lg.languageIds::text[] &&  string_to_array(:#{#cond.languages} , ','))
        	GROUP BY u.id, ta.id, lg.languages,c.categories
        """;

    @Query(
        value = QUERY_SEARCH_BROWSE_ADVISORS,
        countQuery = "SELECT count(1) from (" + QUERY_SEARCH_BROWSE_ADVISORS + ") as temp",
        nativeQuery = true
    )
    Page<IResponseSearchAdvisor> searchBrowseAdvisorByConditions(
        @Param("cond") RequestSearchAdvisor cond,
        UUID communityPartnerId,
        Pageable pageable
    );

    @Query(
        "SELECT ta.id AS id, u.firstName AS firstName, u.lastName AS lastName, u.imageUrl AS imageUrl, u.email AS email, u.country AS country, u.address1 AS address1, " +
        "u.address2 AS address2, u.city AS city, u.state AS state, u.zipCode AS zipCode, u.bio AS bio, u.appt AS appt, u.organization AS organization, " +
        "u.title AS title, u.personalWebsite AS personalWebsite, u.tiktokProfile AS tiktokProfile, u.linkedInProfile AS linkedInProfile, " +
        "u.instagramProfile AS instagramProfile, u.facebookProfile AS facebookProfile, u.twitterProfile AS twitterProfile, u.status as profileStatus " +
        "FROM TechnicalAdvisor ta " +
        "JOIN ta.user u " +
        "WHERE ta.id = :id"
    )
    IResponseTechnicalAdvisorDetail getTechnicalAdvisorById(final UUID id);

    Optional<TechnicalAdvisor> findByUserId(UUID userId);

    @Query(
        value = "SELECT ta.* FROM technical_advisor ta " + "INNER JOIN jhi_user u ON ta.user_id = u.id " + "WHERE u.id = :userId",
        nativeQuery = true
    )
    Optional<TechnicalAdvisor> findIncludeDeleteByUserId(UUID userId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE technical_advisor SET is_delete = true WHERE id = :id", nativeQuery = true)
    void deleteTechnicalAdvisorById(UUID id);

    @Query(
        value = "SELECT ta.communityPartner.id AS communityPartnerId, u.status AS status, " +
        "COALESCE(json_agg(p.id) FILTER (WHERE p.id IS NOT NULL), '[]') AS portalIds " +
        "FROM TechnicalAdvisor ta " +
        "JOIN ta.user u " +
        "LEFT JOIN ta.portals p " +
        "WHERE ta.id = :technicalAdvisorId " +
        "GROUP BY ta.id, ta.communityPartner.id, u.status"
    )
    IResponseTechnicalAdvisorSetting getTechnicalAdvisorSetting(UUID technicalAdvisorId);

    List<TechnicalAdvisor> getAllByCommunityPartnerId(UUID communityPartnerId);

    @Query(
        value = "SELECT ta.id as id, u.firstName as firstName, u.lastName as lastName, u.email as email," +
        " u.status as status, u.normalizedFullName as normalizedFullName, u.imageUrl as imageUrl, au.name AS role " +
        "FROM User u " +
        "JOIN u.authorities au " +
        "LEFT JOIN TechnicalAdvisor ta ON u.id = ta.user.id " +
        "WHERE (ta.communityPartner.id =  :communityPartnerId OR u.communityPartner.id = :communityPartnerId) " +
        " AND (:status is null or u.status = :status)"
    )
    List<IResponseTechnicalAdvisor> getTechnicalAdvisorCommunityPartner(UUID communityPartnerId, UserStatusEnum status);

    @Query( value = """
            SELECT ta
            FROM TechnicalAdvisor ta
            JOIN ta.user u
            WHERE ta.communityPartner.id =  :communityPartnerId
        """
    )
    List<TechnicalAdvisor> getTechnicalAdvisorByCommunityPartnerId(UUID communityPartnerId);

    @Query(
        "SELECT CASE WHEN COUNT(ta) > 0 THEN TRUE ELSE FALSE END " +
        "FROM TechnicalAdvisor ta JOIN ta.portals p " +
        "WHERE ta.user.id = :userId AND p.id = :portalId"
    )
    boolean existsByUserIdAndPortalId(UUID userId, UUID portalId);

    @Query(
        "SELECT count(t) > 0 " + "FROM TechnicalAdvisor t " + "JOIN t.portals p " + "WHERE p.id = :portalId and t.id = :technicalAdvisorId"
    )
    boolean existsByPortalIdAndTechnicalAdvisorId(UUID portalId, UUID technicalAdvisorId);

    @Query("select p from TechnicalAdvisor ta join ta.user u join  ta.portals p where u.id = :userId")
    List<Portal> findAllPortalByUserIdOfTa(UUID userId);

    Optional<TechnicalAdvisor> findByIdAndCommunityPartnerId(UUID id, UUID communityPartnerId);

    @Query(value = """
                SELECT DISTINCT ta.id AS id, u.normalizedFullName AS name
                    FROM TechnicalAdvisor ta
                    JOIN User u ON u.id = ta.user.id
                    JOIN UserAnswerForm uaf ON uaf.entryId = u.id
                    JOIN Question q ON q.id = uaf.questionId
                    JOIN BookingSetting b ON b.user.id = u.id
                    JOIN AdvisementCategory ac ON ac.user.id = u.id
                    JOIN SpecialtyUser  su ON su.user.id = u.id
                    WHERE ta.communityPartner.id = :communityPartnerId
                        AND u.status = :status
                        AND q.formCode = :formCode
                        AND uaf.entryType = :entryType
        """)
    List<IResponseAssignAdvisor> getAssignedAdvisorsByCommunityPartnerId(UUID communityPartnerId, UserStatusEnum status, FormCodeEnum formCode, EntryTypeEnum entryType);

    @Query("""
        select ta.id as id, u.normalizedFullName as normalizedFullName
        from  TechnicalAdvisor ta
        join User u on u.id = ta.user.id
        join Appointment a on a.technicalAdvisor.id = ta.id
        where a.id = :appointmentId
    """)
    Optional<IResponseAdvisorByAppointment> getTechnicalAdvisorByAppointmentId(UUID appointmentId);

    String QUERY_GET_RECOMMEND_ADVISOR = """
        select u.id as userId, u.normalized_full_name as fullName, ta.id as technicalAdvisorId, u.image_url as imageUrl, c.categories as categories
        from technical_advisor ta
        join technical_assistance_advisor taa on taa.technical_advisor_id = ta.id and taa.technical_assistance_submit_id = :technicalAssistanceSubmitId
        join jhi_user u on u.id = ta.user_id and ta.is_delete is false
        left join (
            select ac.user_id,
                jsonb_path_query_array(jsonb_agg(distinct jsonb_build_object('id',c.id,'name',c.name)),'$[0 to 3]')::TEXT as categories
            from advisement_category ac
            left join category c on ac.category_id = c.id and ac.is_delete is false and c.is_delete is false
            group by ac.user_id) as c on c.user_id = u.id
        where
        	u.status = 'ACTIVE'
        	GROUP BY u.id, ta.id,c.categories
        	ORDER BY u.normalized_full_name asc
        	LIMIT 2
        """;
    @Query(value = QUERY_GET_RECOMMEND_ADVISOR, nativeQuery = true)
    List<IResponseRecommendAdvisor> getRecommendAdvisor(UUID technicalAssistanceSubmitId);

    @Modifying
    @Query(
        value = "DELETE FROM technical_advisor_portal tap WHERE tap.technical_advisor_id IN (:technicalAdvisorIds) AND tap.portal_id IN (:portalIds)",
        nativeQuery = true
    )
    void deleteTechnicalAdvisorPortal(List<UUID> technicalAdvisorIds, List<UUID> portalIds);

    String QUERY_GET_PROFILE_COMPLETION_TECHNICAL_ADVISOR = """
            SELECT
                CASE WHEN COUNT(uaf.entry_id) > 0 THEN TRUE ELSE FALSE END AS demographicsCompleted,
                CASE WHEN COUNT(ac.user_id) > 0 THEN TRUE ELSE FALSE END AS advisementCategoriesCompleted,
                CASE WHEN COUNT(su.user_id) > 0 THEN TRUE ELSE FALSE END AS specialtiesCompleted,
                CASE WHEN COUNT(bs.user_id) > 0 THEN TRUE ELSE FALSE END AS bookingSettingsCompleted
            FROM technical_advisor ta
            LEFT JOIN user_answer_form uaf ON uaf.entry_id = ta.user_id
                AND uaf.is_delete = FALSE AND uaf.entry_type = 'USER'
            LEFT JOIN question q ON q.id = uaf.question_id
                AND q.is_delete = FALSE AND q.form_code = 'DEMOGRAPHICS'
            LEFT JOIN advisement_category ac ON ac.user_id = ta.user_id
                AND ac.is_delete = FALSE
            LEFT JOIN specialty_user su ON su.user_id = ta.user_id
                AND su.is_delete = FALSE
            LEFT JOIN booking_setting bs ON bs.user_id = ta.user_id
                AND bs.is_delete = FALSE
            WHERE ta.id = :technicalAdvisorId
        """;
    @Query(value = QUERY_GET_PROFILE_COMPLETION_TECHNICAL_ADVISOR, nativeQuery = true)
    Optional<IResponseProfileCompletionStatus> getProfileCompletionForTechnicalAdvisor(@Param("technicalAdvisorId") UUID technicalAdvisorId);

}
