package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.ActionTypeEnum;
import com.formos.huub.domain.enums.ScreenTypeEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "user_interaction")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE user_interaction SET is_delete = true WHERE id = ?")
public class UserInteractions extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "portal_id", length = 36)
    private UUID portalId;

    @Column(name = "user_id", length = 36)
    private UUID userId;

    @Column(name = "entry_id", length = 36)
    private UUID entryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "screen_type", length = 50)
    private ScreenTypeEnum screenType;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", length = 50)
    private ActionTypeEnum actionType;

}
