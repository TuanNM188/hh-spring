package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.CommunityBoardRestrictionTypeEnum;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "community_board_user_restriction")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE community_board_user_restriction SET is_delete = true WHERE id = ?")
public class CommunityBoardUserRestriction extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "portal_id")
    private UUID portalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "restriction_type")
    private CommunityBoardRestrictionTypeEnum restrictionType;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Column(columnDefinition = "TEXT")
    private String reason;
}
