package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.ConversationMessageTypeEnum;
import com.formos.huub.domain.enums.ConversationStatusEnum;
import com.formos.huub.domain.enums.ConversationTypeEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "conversation")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE conversation SET is_delete = true WHERE id = ?")
public class Conversation extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "portal_id")
    private UUID portalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "conversation_type", length = 50)
    private ConversationTypeEnum conversationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "conversation_status", length = 50)
    private ConversationStatusEnum status;

    @Enumerated(EnumType.STRING)
    @Column(name = "conversation_message_type", length = 50)
    private ConversationMessageTypeEnum conversationMessageType;

    @OneToMany(mappedBy = "conversation")
    private Set<ConversationUser> conversationUsers;

}
