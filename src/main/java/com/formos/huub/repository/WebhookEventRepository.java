package com.formos.huub.repository;

import com.formos.huub.domain.entity.WebhookEvent;
import com.formos.huub.domain.enums.WebhookEventStatusEnum;
import com.formos.huub.domain.enums.ProviderEnum;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, UUID> {

    List<WebhookEvent> findTop10ByProviderAndStatusOrderByCreatedDateDesc(ProviderEnum provider, WebhookEventStatusEnum status);
    List<WebhookEvent> findTop10ByProviderAndStatusAndNextRetryAtIsBeforeOrderByCreatedDateDesc(ProviderEnum provider, WebhookEventStatusEnum status, Instant nextRetryAt);

    @Modifying
    @Transactional
    @Query("DELETE FROM WebhookEvent e WHERE e.createdDate < :dateThreshold and e.status = 'SUCCESS'")
    void deleteByTimestampBefore(@Param("dateThreshold") Instant dateThreshold);
}
