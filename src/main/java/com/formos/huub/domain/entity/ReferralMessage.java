package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.ProjectStatusEnum;
import com.formos.huub.domain.response.directmessage.ResponseSearchReferralMessage;
import com.formos.huub.domain.response.project.ResponseSearchProject;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "referral_message")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE referral_message SET is_delete = true WHERE id = ?")
@SqlResultSetMapping(
    name = "search_referral_messages",
    classes = @ConstructorResult(
        targetClass = ResponseSearchReferralMessage.class,
        columns = {
            @ColumnResult(name = "id", type = UUID.class),
            @ColumnResult(name = "conversationId", type = UUID.class),
            @ColumnResult(name = "communityPartnerId", type = UUID.class),
            @ColumnResult(name = "communityPartnerName", type = String.class),
            @ColumnResult(name = "sendAt", type = Instant.class),
            @ColumnResult(name = "message", type = String.class),
            @ColumnResult(name = "responseMessage", type = String.class)
        }
    )
)
public class ReferralMessage extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "message")
    private String message;

    @Column(name = "is_response")
    private Boolean isResponse;

    @Column(name = "message_response")
    private String messageResponse;

    @Column(name = "send_at")
    private Instant sendAt;

    @Column(name = "response_at")
    private Instant responseAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_owner_id", referencedColumnName = "id")
    private BusinessOwner businessOwner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User primaryUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_partner_id", referencedColumnName = "id")
    private CommunityPartner communityPartner;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", referencedColumnName = "id")
    private Conversation conversation;
}
