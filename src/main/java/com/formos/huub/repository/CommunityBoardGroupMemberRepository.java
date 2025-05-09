package com.formos.huub.repository;

import com.formos.huub.domain.entity.CommunityBoardGroupMember;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.enums.CommunityBoardGroupRoleEnum;
import com.formos.huub.domain.response.communityboard.IResponseGroupInvite;
import com.formos.huub.domain.response.communityboard.IResponseInviteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityBoardGroupMemberRepository extends JpaRepository<CommunityBoardGroupMember, UUID> {
    @Query("""
        SELECT u.normalizedFullName
        FROM CommunityBoardGroupMember c
        JOIN User u on u.id = c.userId
        WHERE c.groupId = :groupId and c.userId in :userIds
    """)
    List<String> findAllUserByUserIdsAndGroupId(List<UUID> userIds, UUID groupId);

    @Query("""
        SELECT EXISTS (
            SELECT 1
            FROM CommunityBoardGroupMember c
            JOIN User u on u.id = c.userId
            WHERE c.groupId = :groupId and c.userId = :userId and c.isDelete = false
        )
    """)
    Boolean existsByUserIdsAndGroupId(UUID userId, UUID groupId);

    @Query("""
        SELECT EXISTS (
            SELECT 1
            FROM CommunityBoardGroupMember c
            JOIN User u on u.id = c.userId
            WHERE c.groupId = :groupId and c.userId = :userId and c.groupRole in :groupRoles and c.isDelete = false
        )
    """)
    Boolean existsMemberInGroup(UUID userId, UUID groupId, List<CommunityBoardGroupRoleEnum> groupRoles);

    @Query("""
        SELECT u
        FROM CommunityBoardGroupMember cbgm
        JOIN User u on u.id = cbgm.userId and u.status = 'ACTIVE'
        WHERE cbgm.groupId = :groupId and cbgm.groupRole in :groupRoles
    """)
    List<User> findMembersByGroupRoles(UUID groupId, List<CommunityBoardGroupRoleEnum> groupRoles);

    Optional<CommunityBoardGroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);

    @Query("""
        SELECT u.id as id, u.imageUrl as imageUrl, u.normalizedFullName as name, cbgm.createdDate as timeRequest
        FROM CommunityBoardGroupMember cbgm
        join User u on u.id = cbgm.userId
        WHERE cbgm.groupId = :groupId and cbgm.status = 'REQUEST_JOIN' and cbgm.isDelete = false
        order by cbgm.createdDate desc
    """)
    Page<IResponseInviteUser> getMemberRequest(UUID groupId, Pageable pageable);

    @Query("""
        SELECT cbgm.userId
        FROM CommunityBoardGroupMember cbgm
        WHERE cbgm.groupId = :groupId and cbgm.groupRole is not null
            and (:groupRoles is null or cbgm.groupRole in :groupRoles)
            and (:ignoreUserIds is null or cbgm.userId not in :ignoreUserIds)
    """)
    List<UUID> findMembersByGroupRoles(UUID groupId, List<UUID> ignoreUserIds, List<CommunityBoardGroupRoleEnum> groupRoles);

    @Modifying
    @Query("UPDATE CommunityBoardGroupMember cbgm SET cbgm.isCreateGroup = true WHERE cbgm.groupId = :groupId and cbgm.userId = :userId ")
    void enableGroupCreationForMember(UUID groupId, UUID userId);

    @Query("""
        SELECT cbgm.userId as userId, cbgm.groupId as groupId, cbgm.invitedBy as inviteId,
            u.normalizedFullName as inviteName, cbgm.createdDate as createDate, cbg.groupName as groupName
        FROM CommunityBoardGroupMember cbgm
        JOIN CommunityBoardGroup cbg ON cbg.id = cbgm.groupId
        LEFT JOIN User u on (u.id = cbgm.invitedBy OR (cbgm.invitedBy is null and u.login = cbgm.createdBy))
        WHERE cbgm.userId = :userId and cbgm.status = 'SEND_INVITE' and cbgm.groupRole is null
        AND (:portalId IS null OR cbg.portalId = :portalId)
    """)
    Page<IResponseGroupInvite> findAllSendInviteByUserId(UUID userId, UUID portalId, Pageable pageable);
}
