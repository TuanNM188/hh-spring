package com.formos.huub.repository;

import com.formos.huub.domain.entity.CommunityBoardGroup;
import com.formos.huub.domain.enums.CommunityBoardGroupRoleEnum;
import com.formos.huub.domain.response.communityboard.IResponseCommunityBoardGroup;
import com.formos.huub.domain.response.communityboard.IResponseCommunityBoardGroupDetail;
import com.formos.huub.domain.response.communityboard.IResponseSelectOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityBoardGroupRepository extends JpaRepository<CommunityBoardGroup, UUID> {
    Boolean existsByGroupNameIgnoreCase(String groupName);

    Boolean existsByGroupNameIgnoreCaseAndIdNot(String groupName, UUID id);

    @Query(
        "SELECT cbg.id AS id, cbg.groupName as name " +
        "FROM CommunityBoardGroup cbg " +
        "JOIN CommunityBoardGroupMember cbgm on cbg.id = cbgm.groupId and (:groupRoles is null or cbgm.groupRole in :groupRoles) " +
        "WHERE cbg.portalId = :portalId and (:userId is null or cbgm.userId = :userId) " +
        "GROUP BY cbg.id "
    )
    List<IResponseSelectOption> findAllGroupForMemberSelectOption(UUID userId, UUID portalId, List<CommunityBoardGroupRoleEnum> groupRoles);

    @Query("""
        SELECT cbg.groupName as name
        FROM CommunityBoardGroup cbg
        WHERE cbg.id = :id
    """)
    Optional<String> findGroupNameById(UUID id);

    @Modifying
    @Query("UPDATE CommunityBoardGroup cbg SET cbg.lastActive = :currentTime WHERE cbg.id = :id")
    void updateLastActive(UUID id, Instant currentTime);

    @Query("""
        SELECT cbg.id
        FROM CommunityBoardGroup cbg
        JOIN CommunityBoardPost cbp on cbp.groupId = cbg.id and cbp.visibility = 'GROUP' and cbp.isDelete = false
        WHERE cbp.id = :postId
    """)
    Optional<UUID> findGroupIdByPostId(UUID postId);

    @Query("""
        SELECT cbg.id
        FROM CommunityBoardGroup cbg
        JOIN CommunityBoardPost cbp on cbp.groupId = cbg.id and cbp.visibility = 'GROUP' and cbp.isDelete = false
        JOIN CommunityBoardComment cbc on cbc.postId = cbp.id and cbc.isDelete = false
        WHERE cbc.id = :commentId
    """)
    Optional<UUID> findGroupIdByCommentId(UUID commentId);

    @Query("""
        SELECT cbg.id as id, cbg.groupName as groupName, cbg.groupAvatar as groupAvatar, cbg.coverPhoto as coverPhoto,
            cbg.portalId as portalId, cbg.lastActive as lastActive, cbgs.settingValue as privacy, cbgmm.groupRole as yourRole, cbgmm.status as status,
            COALESCE(json_agg(json_build_object('id', u.id, 'avatar', u.imageUrl, 'fullName', u.normalizedFullName)) FILTER (WHERE u.id IS NOT NULL), '[]') as members
        FROM CommunityBoardGroup cbg
        JOIN CommunityBoardGroupSetting cbgs on cbgs.id.communityBoardGroup.id = cbg.id and cbgs.settingKey = 'PRIVACY_OPTIONS'
        LEFT JOIN CommunityBoardGroupMember cbgm on cbgm.groupId = cbg.id and cbgm.isDelete = false and cbgm.groupRole in :groupRoles
        LEFT JOIN User u on u.id = cbgm.userId and u.status = 'ACTIVE'
        LEFT JOIN CommunityBoardGroupMember cbgmm on cbgmm.groupId = cbg.id and cbgmm.userId = :userId and cbgmm.isDelete = false
        WHERE (:portalId is null or cbg.portalId = :portalId)
            and (cbgmm.id is not null or cbgs.settingValue in :settingValues)
        group by cbg.id, cbgs.settingValue, cbgmm.id
    """)
    Page<IResponseCommunityBoardGroup> findAllGroupByPortal(UUID portalId, UUID userId, List<String> settingValues, List<CommunityBoardGroupRoleEnum> groupRoles, Pageable pageable);

    @Query("""
        SELECT cbg.id as id, cbg.groupName as groupName, cbg.groupAvatar as groupAvatar, cbg.coverPhoto as coverPhoto,
            cbg.portalId as portalId, cbg.lastActive as lastActive, cbgs.settingValue as privacy, cbgmm.groupRole as yourRole, cbgmm.status as status,
            COALESCE(json_agg(json_build_object('id', u.id, 'avatar', u.imageUrl, 'fullName', u.normalizedFullName)) FILTER (WHERE u.id IS NOT NULL), '[]') as members
        FROM CommunityBoardGroup cbg
        JOIN CommunityBoardGroupSetting cbgs on cbgs.id.communityBoardGroup.id = cbg.id and cbgs.settingKey = 'PRIVACY_OPTIONS'
        JOIN CommunityBoardGroupMember cbgmm on cbgmm.groupId = cbg.id and cbgmm.userId = :userId and cbgmm.isDelete = false and cbgmm.groupRole in :groupRoles
        LEFT JOIN CommunityBoardGroupMember cbgm on cbgm.groupId = cbg.id and cbgm.isDelete = false and cbgm.groupRole in :groupRoles
        LEFT JOIN User u on u.id = cbgm.userId and u.status = 'ACTIVE'
        WHERE (:portalId is null OR cbg.portalId = :portalId)
        group by cbg.id, cbgs.settingValue, cbgmm.id
    """)
    Page<IResponseCommunityBoardGroup> findMyGroupByPortal(UUID portalId, UUID userId, List<CommunityBoardGroupRoleEnum> groupRoles, Pageable pageable);

    @Query("""
        SELECT cbg.id as id, cbg.groupName as groupName, cbg.groupAvatar as groupAvatar, cbg.coverPhoto as coverPhoto,
            cbg.portalId as portalId, cbg.lastActive as lastActive, cbgs.settingValue as privacy, cbgmm.groupRole as yourRole,
            cbgmm.status as status, cbg.description as description,
            json_strip_nulls(json_build_object('id', u.id, 'avatar', u.imageUrl,
             'username', u.username, 'groupRole', cbgm.groupRole, 'fullName', u.normalizedFullName)) as groupOrganizer,
             COALESCE(json_agg(json_build_object('settingKey', cbgss.settingKey, 'settingValue', cbgss.settingValue)) FILTER (WHERE cbgss.settingValue IS NOT NULL), '[]') as groupSetting
        FROM CommunityBoardGroup cbg
        JOIN CommunityBoardGroupSetting cbgs on cbgs.id.communityBoardGroup.id = cbg.id and cbgs.settingKey = 'PRIVACY_OPTIONS'
        LEFT JOIN CommunityBoardGroupSetting cbgss on cbgss.id.communityBoardGroup.id = cbg.id
        LEFT JOIN CommunityBoardGroupMember cbgmm on cbgmm.groupId = cbg.id and cbgmm.userId = :userId and cbgmm.isDelete = false
        LEFT JOIN CommunityBoardGroupMember cbgm on cbgm.groupId = cbg.id and cbgm.isDelete = false and cbgm.groupRole = 'ORGANIZER' and cbgm.isCreateGroup = true
        LEFT JOIN User u on u.id = cbgm.userId and u.status = 'ACTIVE'
        WHERE cbg.id = :groupId
        group by cbg.id, cbgs.settingValue,cbgmm.id, cbgm.id,u.id
    """)
    Optional<IResponseCommunityBoardGroupDetail> findGroupDetailById(UUID groupId, UUID userId);
}
