package com.formos.huub.repository;

import com.formos.huub.domain.entity.ConversationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationUserRepository extends JpaRepository<ConversationUser, UUID> {
    @Query(
        value = "SELECT string_agg(u.normalized_full_name, ', ') as name" +
            " FROM conversation_user cu left join jhi_user u" +
            " on cu.user_id = u.id" +
            " where cu.conversation_id = :conversationId and cu.is_delete is false" +
            " group by cu.conversation_id",
        nativeQuery = true
    )
    String getNameConversation(@Param("conversationId") UUID conversationId);

    List<ConversationUser> findByConversationIdAndUserId(UUID conversationId, UUID userId);

    void deleteAllByConversationId(UUID conversationId);
}
