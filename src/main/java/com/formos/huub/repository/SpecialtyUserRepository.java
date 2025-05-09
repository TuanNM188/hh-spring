package com.formos.huub.repository;

import com.formos.huub.domain.entity.SpecialtyUser;
import com.formos.huub.domain.response.advisementcategory.IResponseCountAdvisor;
import com.formos.huub.domain.response.specialty.IResponseSpecialtyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpecialtyUserRepository extends JpaRepository<SpecialtyUser, UUID> {

    void deleteAllByUserId(UUID userId);

    @Query(value = "SELECT su.specialtyId AS id, s.name AS name, su.yearsOfExperience AS yearsOfExperience, " +
        "json_agg(su.specialtyAreaId) AS specialtyAreaIds, json_agg(sa.name) as specialtyAreaNames " +
        "FROM SpecialtyUser su " +
        "JOIN Specialty s ON su.specialtyId = s.id AND s.isDelete IS false " +
        "JOIN SpecialtyArea sa ON su.specialtyAreaId = sa.id AND sa.isDelete IS false " +
        "WHERE su.user.id = :userId " +
        "GROUP BY su.specialtyId, s.name, su.yearsOfExperience")
    List<IResponseSpecialtyUser> getAllByUserId(UUID userId);

    @Query("SELECT count(su) > 0 FROM SpecialtyUser su WHERE su.specialtyId = :specialtyId")
    Boolean existBySpecialtyId(UUID specialtyId);

    @Query("SELECT count(su) > 0 FROM SpecialtyUser su WHERE su.specialtyAreaId = :specialtyAreaId")
    Boolean existBySpecialtyAreaId(UUID specialtyAreaId);

    @Query(value = "select su.specialty_id as id , count(distinct su.user_id) as numAdvisor " +
        " from specialty_user su " +
        " join specialty s on s.id = su.specialty_id and s.is_delete is false and su.is_delete is false " +
        " join jhi_user u on u.id = su.user_id " +
        " join technical_advisor ta on ta.user_id = u.id and u.status = 'ACTIVE' and ta.is_delete is false " +
        " left join technical_advisor_portal tap on ta.id = tap.technical_advisor_id " +
        " left join community_partner cp on ta.community_partner_id = cp.id and cp.status ='ACTIVE' " +
        " left join portal_community_partner pcp ON pcp.community_partner_id  = cp.id" +
        " left join user_answer_form uafDemographics on uafDemographics.entry_id = u.id and uafDemographics.entry_type = 'USER'" +
        " left join question qa on uafDemographics.question_id = qa.id and qa.form_code = 'DEMOGRAPHICS'" +
        " left join booking_setting b on b.user_id = u.id" +
        " where " +
        " u.status = 'ACTIVE' and uafDemographics.id IS NOT NULL and b.id IS NOT NULL" +
        " and (:portalId is null OR (tap.portal_id = :portalId OR pcp.portal_id = :portalId) )" +
        " and (:communityPartnerId is null or ta.community_partner_id = :communityPartnerId)" +
        " and (:technicalAssistanceId is null or ta.id in (select taa.technical_advisor_id" +
        "     from technical_assistance_advisor taa join technical_assistance_submit tas on taa.technical_assistance_submit_id = tas.id" +
        "     where tas.id = :technicalAssistanceId" +
        " )) " +
        " group by su.specialty_id", nativeQuery = true)
    List<IResponseCountAdvisor> countAdvisorUsingSpecialty(UUID portalId, UUID communityPartnerId, UUID technicalAssistanceId);

    @Query(value = "select su.specialty_area_id as id, count(distinct su.user_id) as numAdvisor " +
        " from specialty_user su " +
        " join specialty_area sa on sa.id = su.specialty_area_id and sa.is_delete is false and su.is_delete is false " +
        " join jhi_user u on u.id = su.user_id " +
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
        " group by su.specialty_area_id, su.specialty_id ", nativeQuery = true)
    List<IResponseCountAdvisor> countAdvisorUsingSpecialtyArea(UUID portalId, UUID communityPartnerId, UUID technicalAssistanceId);
}
