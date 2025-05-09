package com.formos.huub.repository.impl;

import com.formos.huub.domain.entity.ClientNote;
import com.formos.huub.domain.request.clientnote.RequestSearchClientNotes;
import com.formos.huub.domain.response.clientnote.ResponseSearchClientNote;
import com.formos.huub.repository.ClientNotesRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.util.ObjectUtils;

import java.util.Objects;
import java.util.UUID;

public class ClientNotesRepositoryCustomImpl implements ClientNotesRepositoryCustom {

    private final EntityManager em;

    @Autowired
    public ClientNotesRepositoryCustomImpl(JpaContext context) {
        this.em = context.getEntityManagerByManagedType(ClientNote.class);
    }

    @Override
    public Page<ResponseSearchClientNote> searchClientNotesByBusinessOwnerId(
        UUID businessOwnerId, RequestSearchClientNotes request,
        Pageable pageable
    ) {

        StringBuilder sql = new StringBuilder();

        StringBuilder sqlCount = new StringBuilder("SELECT COUNT(1) ");
        StringBuilder sqlSelect = new StringBuilder("SELECT * ");
        sql.append("""
            FROM ( SELECT
                cn.id AS id,
                ju.normalized_full_name AS createdBy,
                cn.note AS note,
                cn.created_date AS createdDate,
                CASE WHEN COUNT(ea.id) > 0 THEN 'Yes' ELSE 'No' END AS hasAttachments
            FROM client_note cn
            JOIN jhi_user ju ON ju.email = cn.created_by AND ju.is_delete = false
            LEFT JOIN entity_attachment ea ON ea.entity_id = cn.id
            WHERE cn.is_delete = false
        """);

        if (Objects.nonNull(businessOwnerId)) {
            sql.append(" AND cn.business_owner_id = :businessOwnerId ");
        }

        sql.append(" GROUP BY cn.id, ju.normalized_full_name, cn.note, cn.created_date ");

        sql.append(") as temp ");

        if (!ObjectUtils.isEmpty(request.getSearchConditions())) {
            sql.append(" WHERE ");
            var sqlCondition = SqlQueryBuilder.generateSqlQuery(request.getSearchConditions());
            sql.append(sqlCondition);
        }

        sqlCount.append(sql);
        sqlSelect.append(sql);
        sqlSelect.append(" ORDER BY ").append(pageable.getSort().toString().replace(":", ""));
        Query queryCount = em.createNativeQuery(sqlCount.toString());
        Query querySelect = em.createNativeQuery(sqlSelect.toString(), "search_client_note");
        if (Objects.nonNull(businessOwnerId)) {
            queryCount.setParameter("businessOwnerId", businessOwnerId);
            querySelect.setParameter("businessOwnerId", businessOwnerId);
        }

        sqlCount.append(sql);
        sqlSelect.append(sql);

        long total = (Long) queryCount.getSingleResult();
        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        var results = querySelect.getResultList();
        return new PageImpl<>(results, pageable, total);
    }


}
