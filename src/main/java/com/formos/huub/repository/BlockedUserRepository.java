package com.formos.huub.repository;

import com.formos.huub.domain.entity.BlockedUser;
import com.formos.huub.domain.enums.ConversationTypeEnum;
import com.formos.huub.domain.response.blockeduser.IResponseBlockedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser, UUID> {


    Optional<BlockedUser> findByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);

    Boolean existsByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);

    void deleteByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);

    @Query(
        value = """
         WITH conversationCm AS (
              SELECT cu1.user_id AS blockerId, cu2.user_id AS blockedId, cu1.conversation_id AS conversationId
              FROM conversation_user cu1 JOIN conversation_user cu2 ON cu1.conversation_id = cu2.conversation_id
              JOIN conversation c ON c.id = cu1.conversation_id
              WHERE cu1.user_id = :blockerId AND c.conversation_type = 'SINGLE'
              AND cu1.is_delete IS FALSE AND cu2.is_delete IS FALSE AND c.is_delete IS FALSE
        )
      SELECT
          b.blocker_id AS blockerId, b.blocked_id AS blockedId, u.normalized_full_name AS blockedName,
          b.created_date AS blockedDate, c.conversationId AS conversationId
      FROM blocked_user b JOIN jhi_user u ON b.blocked_id = u.id AND u.is_delete IS FALSE
      LEFT JOIN conversationCm c ON b.blocker_id = c.blockerId AND b.blocked_id = c.blockedId
      WHERE b.blocker_id = :blockerId AND b.is_delete IS FALSE ORDER BY b.created_date DESC
     """, nativeQuery = true)
    List<IResponseBlockedUser> findBockedUserByBlockerId(@Param("blockerId") UUID blockerId);


    @Query(value = """
                SELECT EXISTS (select 1 from BlockedUser bu
                 where (bu.blocker.id = :blockerId and bu.blocked.id = :blockedId)
                  or (bu.blocked.id = :blockerId and bu.blocker.id = :blockedId))
        """)
    boolean existsByBlocker(UUID blockerId, UUID blockedId);
}
