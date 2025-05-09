package com.formos.huub.repository;

import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.request.member.RequestGetUserInPortal;
import com.formos.huub.domain.request.member.RequestSearchMember;
import com.formos.huub.domain.request.member.RequestSearchMemberInGroup;
import com.formos.huub.domain.response.communityboard.IResponseInviteUser;
import com.formos.huub.domain.response.communityboard.IResponseMentionUser;
import com.formos.huub.domain.response.member.IResponseSearchGroupMember;
import com.formos.huub.domain.response.member.IResponseSearchMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MemberRepository extends JpaRepository<User, UUID>, MemberRepositoryCustom {
    @Transactional
    @Modifying
    @Query(value = "UPDATE jhi_user SET is_delete = true, activated = false WHERE id = :id", nativeQuery = true)
    void deleteMemberById(UUID id);

    @Query(
        "SELECT u.id AS id, CASE WHEN COALESCE(sd2.isPublic, TRUE) = TRUE THEN u.normalizedFullName ELSE u.firstName END AS normalizedFullName," +
            " u.organization AS organization, " +
            " au.name AS role, coalesce(f.status, 'DISCONNECTED') AS followStatus, u.status as status, ph.isPrimary AS isPortalHostPrimary," +
            " fl.followers AS followers, u.email AS email, ulh.lastModifiedDate AS lastActivity, u.imageUrl as imageUrl, u.username as username" +
            " FROM User u JOIN u.authorities au " +
            " LEFT JOIN PortalHost ph ON u.id = ph.userId " +
            " LEFT JOIN (" +
            "    SELECT u2.id as userId, (us.settingValue = 'public') AS isPublic" +
            "    FROM UserSetting us" +
            "    JOIN us.id.user u2 " +
            "    JOIN us.id.settingDefinition sd " +
            "    WHERE sd.category = 'PRIVACY' AND us.settingKey = 'LAST_NAME'" +
            ") sd2 ON sd2.userId = u.id" +
            " LEFT JOIN ph.portal php  " +
            " LEFT JOIN (SELECT uf.followed.id as followedId , count(uf.id) as followers from Follow uf" +
            "     where uf.status = 'CONNECTED' group by uf.followed.id) fl ON fl.followedId = u.id " +
            " LEFT JOIN ( " +
            "     SELECT  al.login as login, max(al.lastModifiedDate) AS lastModifiedDate " +
            "     FROM ActivityLog al group by al.login" +
            " ) ulh ON u.login = ulh.login " +
            " LEFT JOIN u.technicalAdvisor.portals pta " +
            " LEFT JOIN u.technicalAdvisor.communityPartner cpta " +
            " LEFT JOIN CommunityPartner cp on cp.id = u.communityPartner.id" +
            " LEFT JOIN cp.portals pcp " +
            " LEFT JOIN BusinessOwner bo on bo.user.id = u.id left join bo.portal pbo " +
            " LEFT JOIN Follow f on f.followed.id = u.id and f.follower.id = :#{#cond.currentUserId}" +
            " LEFT JOIN Follow fled on fled.followed.id = :#{#cond.currentUserId} and fled.follower.id = u.id" +
            " WHERE u.status = 'ACTIVE'" +
            " and (:#{#cond.portalId} IS NULL OR php.id = :#{#cond.portalId} OR pta.id = :#{#cond.portalId} OR pcp.id = :#{#cond.portalId} OR pbo.id = :#{#cond.portalId})" +
            " AND (:#{#cond.communityPartnerId} IS NULL OR cpta.id = :#{#cond.communityPartnerId} OR cp.id = :#{#cond.communityPartnerId})" +
            " AND (:#{#cond.searchKeyword} IS NULL OR LOWER(u.organization) LIKE CONCAT('%', LOWER(:#{#cond.searchKeyword}), '%') " +
            " OR LOWER(u.normalizedFullName) LIKE CONCAT('%', LOWER(:#{#cond.searchKeyword}), '%')" +
            " OR LOWER(u.email) LIKE CONCAT('%', LOWER(:#{#cond.searchKeyword}), '%')) " +
            " AND (:#{#cond.role} IS NULL OR au.name = :#{#cond.role}) " +
            " AND (:#{#cond.excludeRoles} is null OR au.name NOT IN (:#{#cond.excludeRoles}))" +
            " AND (:#{#cond.followedStatus} is null OR coalesce(fled.status, 'DISCONNECTED') = :#{#cond.followedStatus})" +
            " AND (:#{#cond.followerStatus} is null OR coalesce(f.status, 'DISCONNECTED') = :#{#cond.followerStatus})" +
            " GROUP BY u.id, u.normalizedFullName, u.organization, au.name, f.status, u.status, " +
            "         ph.isPrimary, fl.followers, u.email, ulh.lastModifiedDate, u.imageUrl, u.username, sd2.isPublic"
    )
    Page<IResponseSearchMember> searchMemberByConditions(@Param("cond") RequestSearchMember request, Pageable pageable);

    @Query(
        "SELECT u.id AS id, u.normalizedFullName AS normalizedFullName, u.organization AS organization, " +
            " au.name AS role, coalesce(f.status, 'DISCONNECTED') AS followStatus, u.status as status," +
            " u.email AS email, ulh.lastModifiedDate AS lastActivity, u.imageUrl as imageUrl, u.username as username, cbgm.groupRole as groupRole, cbg.id as groupId " +
            " FROM User u JOIN u.authorities au " +
            " JOIN CommunityBoardGroupMember cbgm ON u.id = cbgm.userId and cbgm.groupRole in ('ORGANIZER', 'MODERATOR', 'MEMBER') and cbgm.isDelete = false " +
            " JOIN CommunityBoardGroup cbg on cbg.id = cbgm.groupId and cbg.id = :#{#cond.groupId} and cbg.isDelete = false" +
            " LEFT JOIN ( " +
            "     SELECT  al.login as login, max(al.lastModifiedDate) AS lastModifiedDate " +
            "     FROM ActivityLog al group by al.login" +
            " ) ulh ON u.login = ulh.login " +
            " LEFT JOIN Follow f on f.followed.id = u.id and f.follower.id = :#{#cond.currentUserId}" +
            " WHERE " +
            " (:#{#cond.searchKeyword} IS NULL OR LOWER(u.organization) LIKE :#{#cond.searchKeyword} " +
            " OR LOWER(u.normalizedFullName) LIKE :#{#cond.searchKeyword} " +
            " OR u.login LIKE :#{#cond.searchKeyword}) " +
            " AND (:#{#cond.portalRole} IS NULL OR au.name = :#{#cond.portalRole}) " +
            " AND (:#{#cond.groupRole} IS NULL OR cbgm.groupRole = :#{#cond.groupRole}) " +
            " GROUP BY u.id, au.name, ulh.lastModifiedDate, f.status, cbgm.groupRole, cbg.id"
    )
    Page<IResponseSearchGroupMember> searchGroupMemberByConditions(@Param("cond") RequestSearchMemberInGroup request, Pageable pageable);

    @Query(
        "SELECT distinct u " +
            " FROM User u JOIN u.authorities au " +
            " LEFT JOIN PortalHost ph ON u.id = ph.userId " +
            " LEFT JOIN u.technicalAdvisor.portals pta " +
            " LEFT JOIN CommunityPartner cp on cp.id = u.communityPartner.id " +
            " LEFT join cp.portals pcp " +
            " LEFT JOIN BusinessOwner bo on bo.user.id = u.id " +
            " LEFT join bo.portal pbo " +
            " where u.status = 'ACTIVE'" +
            " AND (:portalIds IS NULL OR ph.portal.id IN (:portalIds) OR pta.id IN (:portalIds) OR pcp.id IN (:portalIds) OR pbo.id IN (:portalIds))"
    )
    List<User> getAllUserInPortal(List<UUID> portalIds);

    @Query(
        "SELECT distinct u " +
        " FROM User u JOIN u.authorities au " +
        " LEFT JOIN PortalHost ph ON u.id = ph.userId " +
        " LEFT JOIN u.technicalAdvisor.portals pta " +
        " LEFT JOIN CommunityPartner cp on cp.id = u.communityPartner.id " +
        " LEFT join cp.portals pcp " +
        " LEFT JOIN BusinessOwner bo on bo.user.id = u.id " +
        " LEFT join bo.portal pbo " +
        " where u.status = 'ACTIVE' AND (:userIds is null or u.id in :userIds) " +
        " AND (:portalId IS NULL OR ph.portal.id = :portalId OR pta.id = :portalId OR pcp.id = :portalId OR pbo.id = :portalId)"
    )
    List<User> getAllUserIdInPortal(UUID portalId, List<UUID> userIds);

    @Query("""
        SELECT distinct u.id as id, u.imageUrl as imageUrl, u.normalizedFullName as name
        FROM User u JOIN u.authorities au
        LEFT JOIN PortalHost ph ON u.id = ph.userId
        LEFT JOIN u.technicalAdvisor.portals pta
        LEFT JOIN CommunityPartner cp on cp.id = u.communityPartner.id
        LEFT join cp.portals pcp
        LEFT JOIN BusinessOwner bo on bo.user.id = u.id
        LEFT join bo.portal pbo
        LEFT JOIN CommunityBoardGroupMember cbgm on cbgm.userId = u.id and cbgm.isDelete = false
        LEFT JOIN Follow f on f.follower.id = u.id and f.status = 'CONNECTED' and f.isDelete = false
        where u.status = 'ACTIVE'
        AND (:#{#cond.ignoreUserId} is null or u.id <> :#{#cond.ignoreUserId})
        AND (:#{#cond.portalId} IS NULL OR ph.portal.id = :#{#cond.portalId} OR pta.id = :#{#cond.portalId} OR pcp.id = :#{#cond.portalId} OR pbo.id = :#{#cond.portalId})
        AND (:#{#cond.portalIds} IS NULL OR ph.portal.id in :#{#cond.portalIds} OR pta.id in :#{#cond.portalIds} OR pcp.id in :#{#cond.portalIds} OR pbo.id in :#{#cond.portalIds})
        AND ((:#{#cond.visibility} = 'GROUP' and cbgm.groupId = :#{#cond.groupId})
            OR (:#{#cond.visibility} = 'MY_CONNECTIONS' and f.followed.id = :#{#cond.postAuthorId})
            OR (:#{#cond.visibility} = 'ALL_MEMBERS')
            OR (:#{#cond.postAuthorId} IS NULL OR u.id = :#{#cond.postAuthorId})
        )
        """)
    List<IResponseMentionUser> getAllUserInPortalByCondition(@Param("cond") RequestGetUserInPortal request);

    @Query("""
        SELECT u.id as id, u.imageUrl as imageUrl, u.normalizedFullName as name
        FROM User u JOIN u.authorities au
        LEFT JOIN PortalHost ph ON u.id = ph.userId
        LEFT JOIN u.technicalAdvisor.portals pta
        LEFT JOIN CommunityPartner cp on cp.id = u.communityPartner.id
        LEFT join cp.portals pcp
        LEFT JOIN BusinessOwner bo on bo.user.id = u.id
        LEFT join bo.portal pbo
        LEFT JOIN CommunityBoardGroupMember cbgm on cbgm.userId = u.id and cbgm.groupId = :#{#cond.groupId} and cbgm.isDelete = false
        where u.status = 'ACTIVE' and cbgm.id is null
            AND (:#{#cond.ignoreUserId} is null or u.id <> :#{#cond.ignoreUserId})
            AND (:#{#cond.searchKeyword} IS NULL OR LOWER(u.normalizedFullName) LIKE CONCAT('%', :#{#cond.searchKeyword}, '%') OR u.login LIKE CONCAT('%', :#{#cond.searchKeyword}, '%'))
            AND (:#{#cond.portalId} IS NULL OR ph.portal.id = :#{#cond.portalId} OR pta.id = :#{#cond.portalId} OR pcp.id = :#{#cond.portalId} OR pbo.id = :#{#cond.portalId})
        group by u.id
        """)
    Page<IResponseInviteUser> getAllMemberToInviteGroup(@Param("cond") RequestGetUserInPortal request, Pageable pageable);

    @Query("""
        SELECT distinct u.id as id
        FROM User u JOIN u.authorities au
        LEFT JOIN PortalHost ph ON u.id = ph.userId
        LEFT JOIN u.technicalAdvisor.portals pta
        LEFT JOIN CommunityPartner cp on cp.id = u.communityPartner.id
        LEFT join cp.portals pcp
        LEFT JOIN BusinessOwner bo on bo.user.id = u.id
        LEFT join bo.portal pbo
        LEFT JOIN CommunityBoardGroupMember cbgm on cbgm.userId = u.id and cbgm.isDelete = false
        LEFT JOIN Follow f on f.follower.id = u.id and f.status = 'CONNECTED' and f.isDelete = false
        where u.status = 'ACTIVE'
        AND (:ignoreMemberIds is null or u.id not in :ignoreMemberIds)
        AND (:portalId IS NULL OR ph.portal.id = :portalId OR pta.id = :portalId OR pcp.id = :portalId OR pbo.id = :portalId)
        AND ((:visibility = 'GROUP' and cbgm.groupId = :groupId)
            OR (:visibility = 'MY_CONNECTIONS' and f.followed.id = :postAuthorId)
            OR (:visibility = 'ALL_MEMBERS')
            OR (u.id = :postAuthorId)
        )
        """)
    List<UUID> getAllUserIdCanViewPost(UUID portalId, String visibility, UUID groupId, UUID postAuthorId, List<UUID> ignoreMemberIds);

    @Query(
        "SELECT distinct u " +
            " FROM User u JOIN u.authorities au " +
            " LEFT JOIN PortalHost ph ON u.id = ph.userId " +
            " LEFT JOIN u.technicalAdvisor.portals pta " +
            " LEFT JOIN CommunityPartner cp on cp.id = u.communityPartner.id " +
            " LEFT join cp.portals pcp " +
            " LEFT JOIN BusinessOwner bo on bo.user.id = u.id " +
            " LEFT join bo.portal pbo " +
            " where u.status = 'ACTIVE'" +
            " AND (:portalId IS NULL OR ph.portal.id = :portalId OR pta.id = :portalId OR pcp.id = :portalId OR pbo.id = :portalId)" +
            " AND lower(u.normalizedFullName) like concat('%', lower(:searchKeyword),'%') "
    )
    List<User> getAllUserInPortalByKeyword(UUID portalId, String searchKeyword);

    @Query(
        "SELECT distinct u " +
            " FROM User u JOIN u.authorities au " +
            " LEFT JOIN PortalHost ph ON u.id = ph.userId " +
            " where u.status = 'ACTIVE'" +
            " AND ((au.name = 'ROLE_PORTAL_HOST' and ph.portal.id = :portalId) OR au.name IN ('ROLE_SYSTEM_ADMINISTRATOR')) "
    )
    List<User> getAllProgramManager(UUID portalId);

    @Query(
        "SELECT distinct u.id " +
            " FROM User u JOIN u.authorities au " +
            " LEFT JOIN PortalHost ph ON u.id = ph.userId " +
            " where u.status = 'ACTIVE'" +
            " AND au.name = 'ROLE_PORTAL_HOST' and ph.portal.id = :portalId "
    )
    List<UUID> getAllPortalHostByPortalId(UUID portalId);
    @Query(
        "SELECT distinct u.id " +
            " FROM User u JOIN u.authorities au " +
            " LEFT JOIN PortalHost ph ON u.id = ph.userId " +
            " where u.status = 'ACTIVE'" +
            " AND au.name IN ('ROLE_SYSTEM_ADMINISTRATOR') "
    )
    List<UUID> getAllSystemAdminId();

    @Query("select u from User u join PortalHost ph on ph.userId = u.id join ph.portal p where p.id = :portalId and u.status = 'ACTIVE' ")
    List<User> getAllPortalHostInPortal(UUID portalId);

    @Query("""
        SELECT EXISTS (
            select 1
            from User u join u.authorities au
            where au.name = :role and u.id = :userId AND u.status = 'ACTIVE')
    """)
    Boolean existsUserByRole(UUID userId, String role);
}
