package com.formos.huub.domain.entity;

import com.formos.huub.tracker.TrackTranslate;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "community_board_group")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE community_board_group SET is_delete = true WHERE id = ?")
public class CommunityBoardGroup extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "group_name", nullable = false)
    @TrackTranslate
    private String groupName;

    @Column(name = "description", nullable = false)
    @TrackTranslate
    private String description;

    @Column(name = "group_avatar", nullable = false)
    private String groupAvatar;

    @Column(name = "cover_photo", nullable = false)
    private String coverPhoto;

    @Column(name = "portal_id")
    private UUID portalId;

    @Column(name = "last_active")
    private Instant lastActive = Instant.now();
}
