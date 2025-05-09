package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.CommunityBoardGroupRoleEnum;
import com.formos.huub.domain.enums.CommunityBoardGroupStatusEnum;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE community_board_group_member SET is_delete = true WHERE id = ?")
@Table(name = "community_board_group_member")
public class CommunityBoardGroupMember extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "invited_by")
    private UUID invitedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_role")
    private CommunityBoardGroupRoleEnum groupRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CommunityBoardGroupStatusEnum status;

    @Column(name = "is_create_group")
    private Boolean isCreateGroup;
}
