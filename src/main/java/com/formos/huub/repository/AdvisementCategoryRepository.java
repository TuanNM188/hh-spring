package com.formos.huub.repository;

import com.formos.huub.domain.entity.AdvisementCategory;
import com.formos.huub.domain.response.advisementcategory.IResponseAdvisementCategory;
import com.formos.huub.domain.response.advisementcategory.IResponseCountAdvisor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdvisementCategoryRepository extends JpaRepository<AdvisementCategory, UUID> {

    @Query(value = "SELECT ac.categoryId AS id, c.name AS name, ac.yearsOfExperience AS yearsOfExperience, " +
        "json_agg(ac.serviceId) AS serviceIds, json_agg(so.name) as serviceNames " +
        "FROM AdvisementCategory ac " +
        "JOIN Category c ON ac.categoryId = c.id AND c.isDelete IS false " +
        "JOIN ServiceOffered so ON ac.serviceId = so.id AND so.isDelete IS false " +
        "WHERE ac.user.id = :userId " +
        "GROUP BY ac.categoryId, c.name, ac.yearsOfExperience")
    List<IResponseAdvisementCategory> getAllAdvisementCategoryByUserId(UUID userId);

    @Transactional
    @Modifying(flushAutomatically = true)
    @Query(value = "DELETE FROM AdvisementCategory ac WHERE ac.user.id = :userId")
    void deleteAllAdvisementCategoryByUserId(UUID userId);

    @Query("SELECT count(ac) > 0 FROM AdvisementCategory ac WHERE ac.categoryId = :categoryId")
    Boolean existByCategoryId(UUID categoryId);

    @Query("SELECT count(ac) > 0 FROM AdvisementCategory ac WHERE ac.serviceId = :serviceOfferedId")
    Boolean existByServiceOfferedId(UUID serviceOfferedId);

    @Query(value = "select ac.category_id as id, count(distinct ac.user_id) as numAdvisor " +
        " from advisement_category ac  " +
        " join category c on c.id = ac.category_id and c.is_delete is false  and ac.is_delete is false" +
        " join jhi_user u on u.id = ac.user_id " +
        " join technical_advisor ta on ta.user_id = u.id and u.status = 'ACTIVE' and ta.is_delete is false " +
        " left join technical_advisor_portal tap on ta.id = tap.technical_advisor_id " +
        " left join community_partner cp on ta.community_partner_id = cp.id and cp.status ='ACTIVE' " +
        " left join portal_community_partner pcp ON pcp.community_partner_id  = cp.id" +
        " left join user_answer_form uafDemographics on uafDemographics.entry_id = u.id and uafDemographics.entry_type = 'USER'" +
        " left join question qa on uafDemographics.question_id = qa.id and qa.form_code = 'DEMOGRAPHICS'" +
        " left join booking_setting b on b.user_id = u.id" +
        " where " +
        " u.status = 'ACTIVE' and uafDemographics.id IS NOT NULL and b.id IS NOT NULL" +
        " and (:portalId is null OR (tap.portal_id = :portalId OR pcp.portal_id = :portalId)) " +
        " and (:communityPartnerId is null or ta.community_partner_id = :communityPartnerId)" +
        " and (:technicalAssistanceId is null or ta.id in (select taa.technical_advisor_id" +
        "     from technical_assistance_advisor taa join technical_assistance_submit tas on taa.technical_assistance_submit_id = tas.id" +
        "     where tas.id = :technicalAssistanceId" +
        " )) " +
        " group by ac.category_id ", nativeQuery = true)
    List<IResponseCountAdvisor> countAdvisorUsingCategory(UUID portalId, UUID communityPartnerId, UUID technicalAssistanceId);

    @Query(value = " select ac.service_id as id, count(distinct ac.user_id) as numAdvisor " +
        " from advisement_category ac " +
        " join service_offered so on so.id = ac.service_id and so.is_delete is false and ac.is_delete is false" +
        " join jhi_user u on u.id = ac.user_id " +
        " join technical_advisor ta on ta.user_id = u.id and u.status = 'ACTIVE' and ta.is_delete is false " +
        " left join technical_advisor_portal tap on ta.id = tap.technical_advisor_id " +
        " left join community_partner cp on ta.community_partner_id = cp.id and cp.status ='ACTIVE' " +
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
        " group by ac.service_id, ac.category_id", nativeQuery = true)
    List<IResponseCountAdvisor> countAdvisorUsingService(UUID portalId, UUID communityPartnerId, UUID technicalAssistanceId);
}
