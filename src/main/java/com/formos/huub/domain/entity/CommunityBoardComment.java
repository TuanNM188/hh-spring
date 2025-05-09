package com.formos.huub.domain.entity;

import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.tracker.TrackTranslate;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "community_board_comment")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE community_board_comment SET is_delete = true WHERE id = ?")
public class CommunityBoardComment extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "author_id")
    private UUID authorId;

    @Column(name = "post_id")
    private UUID postId;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "content", columnDefinition = "TEXT")
    @TrackTranslate
    private String content;

    @Column(name = "plain_content", columnDefinition = "TEXT")
    private String plainContent;

    @PrePersist
    @PreUpdate
    public void makePlainContent() {
        plainContent = StringUtils.stripHtmlTags(this.content).toLowerCase();
    }
}
