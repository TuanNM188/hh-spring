package com.formos.huub.repository;

import com.formos.huub.domain.entity.TechnicalAssistanceSubmit;
import com.formos.huub.domain.enums.ApprovalStatusEnum;
import com.formos.huub.domain.response.technicaladvisor.IResponseAssignNavigator;
import com.formos.huub.domain.response.technicalassistance.IResponseCountApplication;
import com.formos.huub.domain.response.technicalassistance.IResponseInfoApplication;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TechnicalAssistanceSubmitRepository
    extends JpaRepository<TechnicalAssistanceSubmit, UUID>, TechnicalAssistanceSubmitRepositoryCustom {
    @Query(
        value = "select tas.* from technical_assistance_submit tas where tas.portal_id = :portalId and tas.user_id = :userId" +
        " and tas.program_term_id = :programTermId order by tas.created_date desc limit 1",
        nativeQuery = true
    )
    Optional<TechnicalAssistanceSubmit> findByPortalIdAndUserIdAndProgramTermId(UUID portalId, UUID userId, UUID programTermId);

    @Query(
        value = "select tas.* from technical_assistance_submit tas where tas.program_term_id = :programTermId and tas.user_id = :userId" +
        " and tas.status IN (:status) order by tas.created_date desc limit 1",
        nativeQuery = true
    )
    Optional<TechnicalAssistanceSubmit> findByPortalIdAndUserIdAndStatus(UUID programTermId, UUID userId, List<String> status);

    Optional<TechnicalAssistanceSubmit> findByIdAndProgramTermIdAndStatus(UUID id, UUID programTermId, ApprovalStatusEnum status);

    @Query(
        value = """
        select
        	count( CASE WHEN tas.status = 'SUBMITTED' THEN 1 END) as numSubmitted,
        	count( CASE WHEN tas.status = 'VENDOR_ASSIGNED' THEN 1  END) as numVendorAssigned,
        	count( CASE WHEN tas.status = 'APPROVED' THEN 1 END) as numActive,
        	count( CASE WHEN tas.status = 'DENIED' THEN 1  END) as numDenied,
        	count( CASE WHEN tas.status = 'EXPIRED' THEN 1  END) as numExpired
        from
        	portal p
        	inner join "program" pr on pr.portal_id = p.id and p.status = 'ACTIVE'
        	inner join program_term pt on pr.id = pt.program_id and pt.is_current is true and pt.is_delete is false
        	left join technical_assistance_submit tas ON tas.program_term_id = pt.id
        	 	 and tas.is_delete is false
            left join program_term_vendor ptv on ptv.id = tas.assign_vendor_id
            where (:portalIds is null OR p.id In (:portalIds))
            and (:communityPartnerId is null OR ptv.vendor_id = :communityPartnerId)
            """,
        nativeQuery = true
    )
    IResponseCountApplication countByPortalIdAndStatus(List<UUID> portalIds, UUID communityPartnerId);

    Boolean existsByProgramTermId(UUID programTermId);

    @Query(
        "select tas.id as applicationId , tas.remainingAwardHours as  remainingAwardHours," +
        " u.normalizedFullName as businessOwnerName, pt.startDate as startDate, pt.endDate as endDate" +
        " from TechnicalAssistanceSubmit tas join " +
        " tas.user u" +
        " join tas.programTerm pt join tas.portal p " +
        " left join ProgramTermVendor ptv on ptv.id = tas.assignVendorId " +
        " left join CommunityPartner cp on cp.id = ptv.vendorId " +
        " where pt.status  = 'ACTIVE' and pt.isCurrent is true and tas.status = 'APPROVED' " +
        " and p.id In (:portalIds)" +
        " and (:communityPartnerId is null OR cp.id = :communityPartnerId) "
    )
    List<IResponseInfoApplication> getAllApprovedApplicationByPortal(List<UUID> portalIds, UUID communityPartnerId);

    @Query(
        "select tas.id as applicationId , tas.remainingAwardHours as  remainingAwardHours," +
        " u.normalizedFullName as businessOwnerName, pt.startDate as startDate, pt.endDate as endDate" +
        " from TechnicalAssistanceSubmit tas join " +
        " tas.user u" +
        " join tas.programTerm pt join tas.portal p " +
        " where pt.id  = :programTermId " +
        " and u.id = :userId order by tas.createdDate desc "
    )
    List<IResponseInfoApplication> getAllApplicationByPortalAndUser(UUID programTermId, UUID userId);

    @Query(
        value = "select tas.id as applicationId , tas.remaining_award_hours as  remainingAwardHours," +
        " u.normalized_full_name as businessOwnerName, pt.start_date as startDate, " +
        " pt.end_date as endDate, tas.created_date as createdDate," +
        " u.first_name as firstName, u.last_name as lastName, u.email as email, u.phone_number as phoneNumber, u.id as userId " +
        " from technical_assistance_submit tas " +
        " inner join jhi_user u on tas.user_id = u.id" +
        " inner join program_term pt on pt.id = tas.program_term_id and tas.is_delete is false" +
        " join portal p on p.id = tas.portal_id " +
        " where pt.status  = 'ACTIVE' and pt.is_current is true" +
        " and (coalesce(:startDate, null) is null Or TO_CHAR(tas.created_date AT TIME ZONE 'UTC' AT TIME ZONE :timezone, 'YYYY-MM-DD')  between :startDate and :endDate) " +
        " and p.id = :portalId ",
        nativeQuery = true
    )
    List<IResponseInfoApplication> getAllApplicationByPortal(UUID portalId, String startDate, String endDate, String timezone);

    List<TechnicalAssistanceSubmit> getAllByProgramTermId(UUID programTermId);

    String QUERY_GET_ASSIGN_NAVIGATOR =
        """
        select u.id as userId, u.normalized_full_name as fullName, u.image_url as imageUrl,
            cp.id as communityPartnerId, cp.name as communityPartnerName, cp.image_url as communityPartnerLogo
        from technical_assistance_submit tas
        join program_term_vendor ptv on ptv.id = tas.assign_vendor_id and ptv.is_delete = false
        join community_partner cp ON cp.id = ptv.vendor_id and cp.is_delete = false
        join jhi_user u on u.community_partner_id = cp.id and u.is_navigator = true and u.is_delete is false
        where
        	u.status = 'ACTIVE'
        	and tas.id = :technicalAssistanceSubmitId
        """;

    @Query(value = QUERY_GET_ASSIGN_NAVIGATOR, nativeQuery = true)
    Optional<IResponseAssignNavigator> getAssignNavigator(UUID technicalAssistanceSubmitId);

    @Query("select count(1) > 0 as isExist from TechnicalAssistanceSubmit tas join tas.user u join tas.programTerm pt " +
        " where pt.id = :programTermId and u.id = :userId and tas.status != 'DENIED'")
    Boolean existsByApplicationActive(UUID userId , UUID programTermId);
}
