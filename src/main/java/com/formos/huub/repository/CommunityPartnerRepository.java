package com.formos.huub.repository;

import com.formos.huub.domain.entity.CommunityPartner;
import com.formos.huub.domain.request.communityresource.RequestSearchCommunityResource;
import com.formos.huub.domain.request.usersetting.RequestSearchAllCommunityPartner;
import com.formos.huub.domain.response.advisementcategory.IResponseCountAdvisor;
import com.formos.huub.domain.response.communitypartner.IResponseCommunityPartner;
import com.formos.huub.domain.response.communityresource.IResponseSearchCommunityResource;
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

@Repository
public interface CommunityPartnerRepository extends JpaRepository<CommunityPartner, UUID>, CommunityPartnerRepositoryCustom {
    String SQL_QUERY_SEARCH_COMMUNITY_RESOURCE =
        "select distinct cp.id as id, cp.name as name, cp.image_url as imageUrl, cp.bio as about" +
        " from community_partner cp " +
        " left join portal_community_partner pcp on pcp.community_partner_id = cp.id " +
        " where cp.is_delete is false and (coalesce(:#{#cond.portalId}) is null OR pcp.portal_id = :#{#cond.portalId}) " +
        " and cp.status = 'ACTIVE'" +
        " and ( coalesce(:#{#cond.serviceTypes}) is null or :#{#cond.serviceTypes} = 'ALL' or" +
        " string_to_array(cp.service_types , ',') && string_to_array(:#{#cond.serviceTypes} , ',') )";

    @Query(
        value = SQL_QUERY_SEARCH_COMMUNITY_RESOURCE,
        countQuery = "select count(1) from (" + SQL_QUERY_SEARCH_COMMUNITY_RESOURCE + ") as temp",
        nativeQuery = true
    )
    Page<IResponseSearchCommunityResource> searchCommunityResource(
        @Param("cond") RequestSearchCommunityResource requestSearchCommunityResource,
        Pageable pageable
    );

    Boolean existsByEmailIgnoreCase(String email);

    @Query(
        "select cp from CommunityPartner cp join cp.portals p" +
        " where cp.status = 'ACTIVE' AND cp.isVendor is true" +
        " AND (:#{#cond.portalId} is null OR p.id = :#{#cond.portalId})" +
        " OR (cp.id IN :#{#cond.includes})" +
        " order by cp.name asc "
    )
    List<CommunityPartner> getAllByCondition(@Param("cond") RequestSearchAllCommunityPartner request);

    @Query("select cp from CommunityPartner cp join cp.portals p where cp.id = :id and p.id = :portalId")
    Optional<CommunityPartner> findByIdAndPortalId(UUID id, UUID portalId);

    @Query("select cp from CommunityPartner cp  join cp.portals p where p.id = :portalId and cp.status = 'ACTIVE'")
    List<CommunityPartner> findAllByPortalsId(UUID portalId);

    @Query("select count(1) as num from CommunityPartner cp join cp.portals p where p.id = :portalId and cp.status = 'ACTIVE'")
    Integer countByPortalId(UUID portalId);

    @Query(
        "SELECT distinct c.id AS id, c.name AS name FROM CommunityPartner c left join c.portals p" +
        " WHERE c.status = 'ACTIVE' and (:portalId is null or p.id = :portalId)"
    )
    List<IResponseCommunityPartner> getAllCommunityPartnerMeta(UUID portalId);

    @Modifying
    @Query(
        value = "DELETE FROM portal_community_partner pcp WHERE pcp.portal_id = :portalId AND pcp.community_partner_id = :communityPartnerId",
        nativeQuery = true
    )
    void deletePortalCommunityPartner(UUID portalId, UUID communityPartnerId);

    @Query("select cp from CommunityPartner cp join ProgramTermVendor v on v.vendorId = cp.id " + " where v.id = :vendorId")
    Optional<CommunityPartner> findByVendorId(UUID vendorId);

    @Query(
        value = "select ta.community_partner_id as id, count(distinct u.id) as numAdvisor from technical_advisor ta  " +
            " join community_partner cp on cp.id = ta.community_partner_id  and cp.status = 'ACTIVE' and ta.is_delete is false " +
            " join jhi_user u on u.id = ta.user_id and u.status = 'ACTIVE' " +
            " left join technical_advisor_portal tap on ta.id = tap.technical_advisor_id " +
            " left join portal_community_partner pcp ON pcp.community_partner_id  = cp.id" +
            " left join user_answer_form uafDemographics on uafDemographics.entry_id = u.id and uafDemographics.entry_type = 'USER'" +
            " left join question qa on uafDemographics.question_id = qa.id and qa.form_code = 'DEMOGRAPHICS'" +
            " left join booking_setting b on b.user_id = u.id" +
            " where " +
            " u.status = 'ACTIVE' and uafDemographics.id IS NOT NULL and b.id IS NOT NULL" +
            " and (:portalId is null OR (tap.portal_id = :portalId OR pcp.portal_id = :portalId))" +
            " and (:communityPartnerId is null or ta.community_partner_id = :communityPartnerId)" +
            " and (:technicalAssistanceId is null or ta.id in (select taa.technical_advisor_id" +
            "     from technical_assistance_advisor taa join technical_assistance_submit tas on taa.technical_assistance_submit_id = tas.id" +
            "     where tas.id = :technicalAssistanceId" +
            " )) " +
            " group by ta.community_partner_id ",
        nativeQuery = true
    )
    List<IResponseCountAdvisor> countAdvisorUsingCommunityPartner(UUID portalId, UUID communityPartnerId, UUID technicalAssistanceId);

    @Query(
        value = """
                SELECT cp.*
                FROM community_partner cp
                JOIN portal_community_partner pcp ON cp.id = pcp.community_partner_id
                WHERE cp.is_vendor = true
                  AND cp.id = :id
                  AND pcp.portal_id = :portalId
        """,
        nativeQuery = true
    )
    Optional<CommunityPartner> findVendorByIdAndPortalId(UUID id, UUID portalId);

    @Query(
        value = """
                        SELECT cp.id as id, cp.name as name FROM community_partner cp join portal_community_partner pcp on cp.id = pcp.community_partner_id
                    WHERE pcp.portal_id = :portalId and cp.is_vendor is true and cp.status = 'ACTIVE'
            """,
        nativeQuery = true)
    List<IResponseCommunityPartner> getAllCommunityPartnerByPortalId(UUID portalId);
}
