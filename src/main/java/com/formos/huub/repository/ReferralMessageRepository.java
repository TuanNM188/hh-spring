package com.formos.huub.repository;

import com.formos.huub.domain.entity.ReferralMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReferralMessageRepository extends JpaRepository<ReferralMessage, UUID>, ReferralMessageRepositoryCustom {

    Optional<ReferralMessage> findByCommunityPartnerIdAndConversationId(UUID communityPartner_id, UUID conversation_id);

    Optional<ReferralMessage> findByConversationId( UUID conversation_id);

    @Query("select rm from ReferralMessage rm where rm.conversation.id = :conversation_id order by rm.createdDate desc")
    List<ReferralMessage> findByConversationIdByReferralMessage(UUID conversation_id);
}
