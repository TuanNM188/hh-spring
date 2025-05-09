package com.formos.huub.repository;

import com.formos.huub.domain.entity.BusinessOwner;
import com.formos.huub.domain.response.member.IResponseBusinessOwnerEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.formos.huub.domain.request.businessowner.RequestSearchBusinessOwner;
import com.formos.huub.domain.response.businessowner.IResponseSearchBusinessOwner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.formos.huub.domain.response.communitypartner.IResponseAssignAdvisor;
import org.springframework.data.repository.query.Param;

@Repository
public interface BusinessOwnerRepository extends JpaRepository<BusinessOwner, UUID> {
    boolean existsByPortalIdAndUserId(UUID portalId, UUID userId);

    Optional<BusinessOwner> findByUserId(UUID userId);

    Optional<BusinessOwner> findByUserEmailIgnoreCase(String email);

    @Query("""
            SELECT bo.id as id, u.email as email
            FROM BusinessOwner bo
            JOIN User u on u.id = bo.user.id
            WHERE u.email in :email
        """)
    List<IResponseBusinessOwnerEmail> findAllByEmails(List<String> email);

    Optional<BusinessOwner> findByIdAndPortalId(UUID id, UUID portalId);

    @Query(value = """
        select bo.id as id, u.normalizedFullName as name, u.id as userId from BusinessOwner bo join User u on bo.user.id = u.id
        where bo.portal.id = :portalId and u.status = 'ACTIVE'
        """)
    List<IResponseAssignAdvisor> getBusinessOwnerByPortalId(UUID portalId);

    String QUERY_GET_BUSINESS_OWNER_BY_DATE = """
             select bo.id as id, u.normalized_full_name as name, u.id as userId,  bo.created_date as createdDate,
             u.first_name as firstName, u.last_name as lastName, u.email as email, u.phone_number as phoneNumber, latest_tas.applicationId as applicationId
             from business_owner bo join jhi_user u on bo.user_id = u.id
         LEFT JOIN (
             SELECT tas.id as applicationId, tas.user_id as userId,
             ROW_NUMBER() OVER (PARTITION BY tas.user_id ORDER BY tas.created_date desc) AS rn
             FROM technical_assistance_submit tas
             WHERE tas.is_delete IS FALSE ) AS latest_tas ON latest_tas.userId = u.id AND latest_tas.rn = 1
         where bo.portal_id = :portalId
         and (coalesce(:startDate, null) is null Or TO_CHAR(bo.created_date AT TIME ZONE 'UTC' AT TIME ZONE :timezone, 'YYYY-MM-DD')  between :startDate and :endDate)
        """;
    @Query(value = QUERY_GET_BUSINESS_OWNER_BY_DATE
        , nativeQuery = true)
    List<IResponseAssignAdvisor> getBusinessOwnerByPortalIdAndDateBetween(UUID portalId, String startDate, String endDate, String timezone);

    String QUERY_SEARCH_BUSINESS_OWNER_BY_CONDITIONS = """
                SELECT DISTINCT
                    ju.normalized_full_name AS businessOwnerName, ju.id AS userId, p.platform_name AS portalName,
                    ju.image_url AS imageUrl, latest_tas.status AS applicationStatus, busInfo.businessName AS businessName,
                    ju.status AS status, bo.created_date AS createdDate, busInfo.searchAnswerContent, busOptionInfo.searchAnswerOptionContent
                FROM business_owner bo
                JOIN jhi_user ju ON bo.user_id = ju.id AND ju.is_delete IS FALSE
                LEFT JOIN portal p ON bo.portal_id = p.id AND p.is_delete IS false
                LEFT JOIN portal_community_partner pcp ON pcp.portal_id = p.id
                LEFT JOIN (
                     SELECT
                         tas.*,
                         ROW_NUMBER() OVER (PARTITION BY tas.user_id ORDER BY tas.created_date DESC) AS rn
                     FROM technical_assistance_submit tas
                     WHERE tas.is_delete IS FALSE
                ) AS latest_tas ON latest_tas.user_id = ju.id AND latest_tas.rn = 1
                LEFT JOIN program_term_vendor ptv ON ptv.id = latest_tas.assign_vendor_id AND ptv.is_delete IS FALSE
                LEFT JOIN technical_assistance_advisor taa ON latest_tas.id = taa.technical_assistance_submit_id
                LEFT JOIN (
                    SELECT
                        uaf.entry_id AS busOptionUserId,
                        STRING_AGG(ao.answer, ', ') AS searchAnswerOptionContent
                    FROM user_answer_form uaf
                    JOIN question q ON uaf.question_id = q.id
                    JOIN user_answer_option ON uaf.id = user_answer_option.user_answer_form_id
                    JOIN answer_option ao ON user_answer_option.option_id = ao.id
                    WHERE uaf.entry_type = 'USER' AND uaf.is_delete IS FALSE
                    AND q.question_code IN (
                        'QUESTION_BUSINESS_INDUSTRY_INFORMATION',
                        'QUESTION_BUSINESS_STRUCTURE'
                    )
                    GROUP BY uaf.entry_id
                ) AS busOptionInfo ON busOptionInfo.busOptionUserId = ju.id
                LEFT JOIN (
                    SELECT
                        uaf.entry_id AS busInfoUserId,
                        MAX(CASE WHEN q.question_code = 'PORTAL_INTAKE_QUESTION_BUSINESS' THEN uaf.additional_answer END) AS businessName,
                        STRING_AGG(uaf.additional_answer, ', ') AS searchAnswerContent
                    FROM user_answer_form uaf
                    JOIN question q ON uaf.question_id = q.id
                    WHERE uaf.entry_type = 'USER' AND uaf.is_delete IS FALSE
                    AND q.question_code IN (
                        'PORTAL_INTAKE_QUESTION_JOB_TITLE',
                        'PORTAL_INTAKE_QUESTION_CITY',
                        'PORTAL_INTAKE_QUESTION_ZIPCODE',
                        'PORTAL_INTAKE_QUESTION_STREET_ADDRESS',
                        'PORTAL_INTAKE_QUESTION_DESCRIBE_TYPE_OF_ASSISTANCE_NEEDED',
                        'PORTAL_INTAKE_QUESTION_BUSINESS'
                    )
                    GROUP BY uaf.entry_id
                ) AS busInfo ON busInfo.busInfoUserId = ju.id
                WHERE bo.is_delete IS false
               AND (    coalesce(:#{#cond.searchKeyword}, null) IS NULL OR
                        ju.normalized_full_name ILIKE coalesce(:#{#cond.searchKeyword}, '')
                        OR ju.email ILIKE coalesce(:#{#cond.searchKeyword}, '')
                        OR busInfo.searchAnswerContent ILIKE coalesce(:#{#cond.searchKeyword}, '')
                        OR busInfo.businessName ILIKE coalesce(:#{#cond.searchKeyword}, '')
                        OR busOptionInfo.searchAnswerOptionContent ILIKE coalesce(:#{#cond.searchKeyword}, '')
               )
                AND (coalesce(:#{#cond.portalId}, null ) IS NULL OR p.id = :#{#cond.portalId})
                AND (coalesce(:#{#cond.technicalAdvisorId}, null ) IS NULL OR taa.technical_advisor_id = :#{#cond.technicalAdvisorId})
                AND (coalesce(:#{#cond.communityPartnerId}, null ) IS NULL OR pcp.community_partner_id = :#{#cond.communityPartnerId})
                AND (coalesce(:#{#cond.technicalAdvisorId}, null ) IS NOT NULL OR (
                    coalesce(:#{#cond.communityPartnerId}, null ) IS NULL OR ptv.vendor_id = :#{#cond.communityPartnerId})
                )
                AND ((COALESCE(:#{#cond.applicationStatus}, '') = '' ) OR latest_tas.status = :#{#cond.applicationStatus})
                GROUP BY ju.normalized_full_name, ju.id, p.platform_name, ju.image_url, latest_tas.status, busInfo.businessName, ju.status, createdDate, searchAnswerContent, searchAnswerOptionContent
        """;

    @Query(value = QUERY_SEARCH_BUSINESS_OWNER_BY_CONDITIONS,
        nativeQuery = true,
        countQuery = "SELECT COUNT(1) FROM ( " + QUERY_SEARCH_BUSINESS_OWNER_BY_CONDITIONS + ") AS temp")
    Page<IResponseSearchBusinessOwner> searchBusinessOwnerByConditions(
        @Param("cond") RequestSearchBusinessOwner request,
        Pageable pageable
    );
}
