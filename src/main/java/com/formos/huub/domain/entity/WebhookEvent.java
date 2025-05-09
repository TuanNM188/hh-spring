package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.WebhookEventStatusEnum;
import com.formos.huub.domain.enums.ProviderEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "webhook_events")
@Where(clause = "is_delete='false'")
@SQLDelete(sql = "UPDATE webhook_events SET is_delete = true WHERE id = ?")
public class WebhookEvent {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "event_id")
    private String eventId;

    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    private ProviderEnum provider;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private WebhookEventStatusEnum status;

    @Column(name = "retry_count", columnDefinition = "int default 0")
    private int retryCount;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private Instant createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;

    @Column(name = "is_delete", columnDefinition = "boolean default false")
    private boolean isDelete;

    @Column(name = "error_detail", columnDefinition = "TEXT")
    private String errorDetail;

    @PrePersist
    public void onCreate() {
        createdDate = Instant.now();
        lastModifiedDate = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        lastModifiedDate = Instant.now();
    }
}
