package com.formos.huub.repository;

import com.formos.huub.domain.entity.Language;
import com.formos.huub.domain.response.advisementcategory.IResponseCountAdvisor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LanguageRepository extends JpaRepository<Language, UUID> {

    @Query("select count(l) > 0 from Language l where lower(l.name) = lower(:name) ")
    Boolean existsByName(String name);

    @Query("select count(l) > 0 from Language l where lower(l.name) = lower(:name) and l.id != :id")
    Boolean existsByNameAndNotEqualId(String name, UUID id);

    @Query(value = "select " +
        " l.id as id, l.name,count(distinct uaf.entry_id ) as numAdvisor " +
        " from user_answer_form uaf " +
        " join question q on uaf.question_id = q.id and q.question_code = 'QUESTION_LANGUAGE_SPOKEN' " +
        "     and uaf.entry_type = 'USER' and uaf.is_delete is false" +
        " left join user_answer_option uao on uaf.id = uao.user_answer_form_id " +
        " join jhi_user u on u.id = uaf.entry_id and u.status = 'ACTIVE' and uao.is_delete is false " +
        " join technical_advisor ta  on u.id = ta.user_id and ta.is_delete is false " +
        " left join technical_advisor_portal tap on ta.id = tap.technical_advisor_id " +
        " left join community_partner cp on ta.community_partner_id = cp.id and cp.status ='ACTIVE' " +
        " left join portal_community_partner pcp ON pcp.community_partner_id  = cp.id" +
        " left join \"language\" l on uao.option_id = l.id and q.option_type = 'LANGUAGE' " +
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
        " group by q.id, l.id ", nativeQuery = true)
    List<IResponseCountAdvisor> countAdvisorUsingLanguage(UUID portalId, UUID communityPartnerId, UUID technicalAssistanceId);
}
