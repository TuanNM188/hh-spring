package com.formos.huub.repository.impl;

import com.formos.huub.domain.entity.ReferralMessage;
import com.formos.huub.domain.request.directmessage.RequestSearchReferralMessage;
import com.formos.huub.domain.response.directmessage.ResponseSearchReferralMessage;
import com.formos.huub.repository.ReferralMessageRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.util.ObjectUtils;

public class ReferralMessageRepositoryCustomImpl implements ReferralMessageRepositoryCustom {

    private final EntityManager em;

    @Autowired
    public ReferralMessageRepositoryCustomImpl(JpaContext context) {
        this.em = context.getEntityManagerByManagedType(ReferralMessage.class);
    }

    @Override
    public Page<ResponseSearchReferralMessage> searchByTermAndCondition(RequestSearchReferralMessage condition, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        StringBuilder sqlCount = new StringBuilder("select count(DISTINCT rm.id) ");
        StringBuilder sqlSelect = new StringBuilder("SELECT * ");
        sql.append(" " +
            "FROM (SELECT " +
            "  rm.id as id,  " +
            "  c.id as conversationId,  " +
            "  cp.id as communityPartnerId, " +
            "  cp.name as communityPartnerName, " +
            "  rm.send_at as sendAt, " +
            "  rm.message AS message, " +
            "  bo.id as businessOwnerId, " +
            "  case when rm.message_response::jsonb ->> 'messageType' = 'MEDIA_IMAGES' then 'Images'" +
            "  when rm.message_response::jsonb ->> 'messageType' = 'GIF' then 'GIFs'" +
            "  when rm.message_response::jsonb ->> 'messageType' = 'ATTACHMENT' then 'Attachments'" +
            "  when rm.message_response::jsonb ->> 'messageType' = 'MEDIA_FILE' then 'Files'" +
            "  else TRIM(BOTH ' ' FROM REGEXP_REPLACE(coalesce(rm.message_response::jsonb ->> 'content', null), :regexHtml, ' ', 'g'))" +
            "  end as responseMessage" +
            "  FROM referral_message rm " +
            "  JOIN community_partner cp on rm.community_partner_id = cp.id " +
            "  JOIN conversation c on c.id = rm.conversation_id " +
            "  LEFT JOIN business_owner bo on bo.id = rm.business_owner_id " +
            "  WHERE rm.is_delete is false) as rm where 1 = 1  ");

        if (!ObjectUtils.isEmpty(condition.getBusinessOwnerId())) {
            sql.append(" AND (rm.businessOwnerId = :businessOwnerId)");
        }
        if (!ObjectUtils.isEmpty(condition.getSearchConditions())) {
            sql.append(" AND ");
            var sqlCondition = SqlQueryBuilder.generateSqlQuery(condition.getSearchConditions());
            sql.append(sqlCondition);
        }
        sqlCount.append(sql);
        sqlSelect.append(sql);
        sqlSelect.append(" ORDER BY ").append(pageable.getSort().toString().replace(":", ""));
        Query queryCount = em.createNativeQuery(sqlCount.toString());
        Query querySelect = em.createNativeQuery(sqlSelect.toString(), "search_referral_messages");

        if (!ObjectUtils.isEmpty(condition.getBusinessOwnerId())) {
            queryCount.setParameter("businessOwnerId", condition.getBusinessOwnerId());
            querySelect.setParameter("businessOwnerId", condition.getBusinessOwnerId());
        }
        if (!ObjectUtils.isEmpty(condition.getRegexHtml())) {
            queryCount.setParameter("regexHtml", condition.getRegexHtml());
            querySelect.setParameter("regexHtml", condition.getRegexHtml());
        }
        long total = (Long) queryCount.getSingleResult();
        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        var results = querySelect.getResultList();
        return new PageImpl<>(results, pageable, total);
    }
}
