package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.CommunityBoardTargetTypeEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "community_board_flag")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE community_board_flag SET is_delete = true WHERE id = ?")
public class CommunityBoardFlag extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    private CommunityBoardTargetTypeEnum targetType;

    @Column(name = "target_id")
    private UUID targetId;

    @Column(name = "performed_id")
    private UUID performedId;

    @Column(name = "performed_at")
    private Instant performedAt = Instant.now();

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "portal_id")
    private UUID portalId;
}
