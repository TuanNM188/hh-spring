package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.CommunityBoardVisibilityEnum;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.tracker.TrackTranslate;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "community_board_post")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE community_board_post SET is_delete = true WHERE id = ?")
public class CommunityBoardPost extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "author_id")
    private UUID authorId;

    @Column(name = "content", columnDefinition = "TEXT")
    @TrackTranslate
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility")
    private CommunityBoardVisibilityEnum visibility;

    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "portal_id")
    private UUID portalId;

    @Column(name = "scheduled_time")
    private Instant scheduledTime;

    @Column(name = "is_pin", columnDefinition = "boolean default false")
    private Boolean isPin = false;

    @Column(name = "is_notify_all")
    private Boolean isNotifyAll;

    @Column(name = "plain_content", columnDefinition = "TEXT")
    private String plainContent;

    @PrePersist
    @PreUpdate
    public void makePlainContent() {
        plainContent = StringUtils.isBlank(this.content) ? "" : StringUtils.stripHtmlTags(this.content).toLowerCase();
    }
}
