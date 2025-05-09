package com.formos.huub.repository;

import com.formos.huub.domain.entity.Conversation;
import com.formos.huub.domain.request.directmessage.RequestSearchConversation;
import com.formos.huub.domain.response.directmessage.IResponseListConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    @Query(value = "SELECT dm.* FROM ( " +
        " SELECT c.id as id, c.name as name, c.conversation_type as conversationType, c.conversation_status as status, c.portal_id as portalId, " +
        " p.platform_name AS portalName, p.primary_color AS portalPrimaryColor, c.conversation_message_type AS conversationMessageType, " +
        " json_agg(json_build_object('userId', cu.user_id, 'name', u.normalized_full_name, 'imageUrl', u.image_url, 'authorities', au.authority_name)) AS userInConversation," +
        " CASE WHEN cuu.status = 'ARCHIVED' THEN true else FALSE END as isArchived, c.created_date as createdDate " +
        " FROM conversation c " +
        " LEFT JOIN conversation_user cu ON c.id = cu.conversation_id " +
        " LEFT JOIN conversation_user cuu ON c.id = cuu.conversation_id and cuu.user_id = :userId" +
        " LEFT JOIN jhi_user u ON cu.user_id = u.id " +
        " LEFT JOIN jhi_user_authority au ON au.user_id = u.id  " +
        " LEFT JOIN portal p ON c.portal_id = p.id " +
        " WHERE c.is_delete = false AND cu.is_delete = false " +
        " GROUP BY c.id, cuu.status, p.platform_name, p.primary_color " +
        " ) AS dm " +
        " WHERE (:userId is NULL OR dm.userInConversation::jsonb @> jsonb_build_array(jsonb_build_object('userId', :userId))) " +
        " AND (CAST(:#{#cond.directUserId} AS VARCHAR) IS NULL OR dm.userInConversation::jsonb @> jsonb_build_array(jsonb_build_object('userId',CAST(:#{#cond.directUserId} AS VARCHAR))))" +
        " AND (CAST(:#{#cond.conversationId} AS VARCHAR) IS NULL OR dm.id = :#{#cond.conversationId})" +
        " AND (CAST(:#{#cond.status} AS VARCHAR) IS NULL OR dm.status = CAST(:#{#cond.status} AS VARCHAR))" +
        " AND (CAST(:#{#cond.conversationType} AS VARCHAR) IS NULL OR dm.conversationType = CAST(:#{#cond.conversationType} AS VARCHAR))"+
        " AND (CAST(:#{#cond.portalId} AS VARCHAR) IS NULL OR dm.portalId = :#{#cond.portalId} OR dm.portalId is null)" +
        " AND (CAST(:#{#cond.isArchived} AS VARCHAR) IS NULL OR dm.isArchived = :#{#cond.isArchived}) " +
        " AND (CAST(:#{#cond.conversationMessageType} AS VARCHAR) IS NULL OR dm.conversationMessageType = CAST(:#{#cond.conversationMessageType} AS VARCHAR))",
        nativeQuery = true)
    List<IResponseListConversation> searchConversationByCondition(@Param("cond") RequestSearchConversation request,@Param("userId") UUID userId);

}
