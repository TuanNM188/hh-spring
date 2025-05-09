package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.NotificationTypeEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "notification")
@Where(clause = "is_delete='false'")
@SQLDelete(sql = "UPDATE notification SET is_delete = true WHERE id = ?")
public class Notification extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "sender_id", length = 36)
    private UUID senderId;

    @Column(name = "user_id", length = 36)
    private UUID userId;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "notification_type")
    @Enumerated(EnumType.STRING)
    private NotificationTypeEnum notificationType;

    @Column(name = "reference_url")
    private String referenceUrl;

    @Column(name = "is_read", columnDefinition = "boolean default false")
    private Boolean isRead;

    @Column(name = "portal_id", length = 36)
    private UUID portalId;

}
