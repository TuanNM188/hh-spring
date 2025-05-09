package com.formos.huub.repository.impl;

import com.formos.huub.domain.entity.Portal;
import com.formos.huub.domain.request.common.SqlConditionRequest;
import com.formos.huub.domain.request.learninglibrary.RequestSearchLearningLibrary;
import com.formos.huub.domain.response.learninglibrary.ResponseSearchLearningLibrary;
import com.formos.huub.domain.response.learninglibrary.ResponseSearchLearningLibraryCardView;
import com.formos.huub.repository.LearningLibraryRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class LearningLibraryRepositoryCustomImpl implements LearningLibraryRepositoryCustom {

    private final EntityManager em;

    @Autowired
    public LearningLibraryRepositoryCustomImpl(JpaContext context) {
        this.em = context.getEntityManagerByManagedType(Portal.class);
    }

    @Override
    public Page<ResponseSearchLearningLibrary> searchByTermAndCondition(RequestSearchLearningLibrary condition, UUID portalId, Pageable pageable) {
        StringBuffer sql = new StringBuffer();
        StringBuilder sqlCount = new StringBuilder("select count(DISTINCT id) ");
        StringBuilder sqlSelect = new StringBuilder("SELECT *  ");
        sql.append(" FROM ( SELECT f.id AS id, " +
            " f.name AS name, f.content_type AS contentType, f.access_type AS accessType, s.numSteps AS numSteps, " +
            " f.status AS status, f.created_date AS createdDate, f.user_created_id AS userCreatedId " +
            " FROM learning_library f left join " +
            " (select ls.learning_library_id, count(ls.id) as numSteps from learning_library_step ls" +
            " where ls.is_delete is false group by ls.learning_library_id ) s " +
            " ON f.id = s.learning_library_id " +
            " left join learning_library_portal llp on f.id =  llp.learning_library_id " +
            " WHERE f.is_delete IS FALSE ");
        if (Objects.nonNull(condition.getPortalId()) || Objects.nonNull(portalId)) {
            sql.append(" AND (llp.portal_id = :portalId)");
        }

        sql.append(" GROUP BY f.id, s.numSteps) as temp ");
        if (!ObjectUtils.isEmpty(condition.getSearchConditions())){
            sql.append(" WHERE ");
            var sqlCondition = SqlQueryBuilder.generateSqlQuery(condition.getSearchConditions());
            sql.append(sqlCondition);
        }
        sqlCount.append(sql);
        sqlSelect.append(sql);
        sqlSelect.append(" ORDER BY ").append(pageable.getSort().toString().replace(":", ""));
        Query queryCount = em.createNativeQuery(sqlCount.toString());
        Query querySelect = em.createNativeQuery(sqlSelect.toString(), "search_learning_library");
        if (Objects.nonNull(condition.getPortalId())) {
            queryCount.setParameter("portalId", condition.getPortalId());
            querySelect.setParameter("portalId", condition.getPortalId());
        } else if (Objects.nonNull(portalId)) {
                queryCount.setParameter("portalId", portalId);
                querySelect.setParameter("portalId", portalId);
        }
        long total = (Long) queryCount.getSingleResult();
        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        var results = querySelect.getResultList();
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<ResponseSearchLearningLibraryCardView> searchByTermAndConditionCardView(RequestSearchLearningLibrary condition, UUID userId, UUID portalId, Pageable pageable) {
        StringBuffer sql = new StringBuffer();
        StringBuilder sqlCount = new StringBuilder("select count(DISTINCT id) ");
        StringBuilder sqlSelect = new StringBuilder("SELECT * ");
        sql.append(" FROM ( SELECT l.id AS id, l.name AS name, l.description AS description, l.hero_image AS thumbnail, " +
            "COUNT(DISTINCT ll.id) AS lessons, COALESCE(user_ll.completion_status, 'NOT_STARTED') AS completionStatus, " +
            "COALESCE(user_ll.is_bookmark, false) AS isBookmark, l.category_id AS categoryId, " +
            "cat.name AS categoryName, cat.icon_url AS categoryIcon, " +
            "COALESCE(ROUND(AVG(ull.rating)), 0) AS rating, l.created_date AS createdDate, l.user_created_id AS userCreatedId " +
            "FROM learning_library l " +
            "JOIN learning_library_step ls ON l.id = ls.learning_library_id AND ls.is_delete IS false " +
            "LEFT JOIN learning_library_lesson ll ON ls.id = ll.learning_library_step_id AND ll.is_delete IS false " +
            "JOIN learning_library_portal lp ON l.id = lp.learning_library_id " +
            "LEFT JOIN user_learning_library user_ll ON l.id = user_ll.learning_library_id AND user_ll.user_id = :userId " +
            "LEFT JOIN user_learning_library ull ON l.id = ull.learning_library_id " +
            "JOIN category cat ON l.category_id = cat.id " +
            "JOIN learning_library_speaker lsp ON l.id = lsp.learning_library_id " +
            "WHERE l.is_delete IS false AND l.status = 'PUBLISHED' ");
        if (Objects.nonNull(portalId)) {
            sql.append(" AND (lp.portal_id = :portalId) ");
        }

        List<SqlConditionRequest> searchConditions = condition.getSearchConditions();
        SqlConditionRequest speakerRequest;
        var speakerOpt = searchConditions.stream().filter(conditionRequest -> conditionRequest.getColumn().equals("speaker")).findFirst();
        speakerRequest = speakerOpt.orElse(null);
        if (Objects.nonNull(speakerRequest)) {
            sql.append(" AND (lsp.speaker_id IN (:speakerId)) ");
        }

        if (speakerRequest != null) {
            searchConditions = searchConditions.stream()
                .filter(conditionRequest -> !conditionRequest.equals(speakerRequest))
                .collect(Collectors.toList());
        }

        sql.append("GROUP BY l.id, l.name, l.description, l.hero_image, l.category_id, l.user_created_id, cat.name, cat.icon_url, l.created_date, user_ll.completion_status, user_ll.is_bookmark) AS temp ");
        if (!ObjectUtils.isEmpty(searchConditions)){
            sql.append(" WHERE ");

            var sqlCondition = SqlQueryBuilder.generateSqlQuery(searchConditions);
            sql.append(sqlCondition);
        }

        sqlCount.append(sql);
        sqlSelect.append(sql);
        sqlSelect.append(" ORDER BY ").append(pageable.getSort().toString().replace(":", ""));
        Query queryCount = em.createNativeQuery(sqlCount.toString());
        Query querySelect = em.createNativeQuery(sqlSelect.toString(), "search_learning_library_card_view");
        queryCount.setParameter("userId", userId);
        querySelect.setParameter("userId", userId);

        if (Objects.nonNull(portalId)) {
            queryCount.setParameter("portalId", portalId);
            querySelect.setParameter("portalId", portalId);
        }

        if (Objects.nonNull(speakerRequest)) {
            queryCount.setParameter("speakerId", UUID.fromString((String) speakerRequest.getValue()));
            querySelect.setParameter("speakerId", UUID.fromString((String) speakerRequest.getValue()));
        }

        long total = (Long) queryCount.getSingleResult();
        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        var results = querySelect.getResultList();
        return new PageImpl<>(results, pageable, total);
    }
}
