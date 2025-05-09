package com.formos.huub.repository;

import com.formos.huub.domain.entity.CommunityBoardPost;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.request.communityboardpost.RequestGetListCommunityBoardPost;
import com.formos.huub.domain.response.communityboardpost.IResponseCommunityBoardPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityBoardPostRepository extends JpaRepository<CommunityBoardPost, UUID> {
    String queryGetPostIds ="""
        SELECT cbp.id AS id
        FROM community_board_post cbp
        LEFT JOIN community_board_comment cbc on cbc.is_delete = false and cbc.post_id = cbp.id
        LEFT JOIN community_board_group_member cbgm ON cbgm.group_id = cbp.group_id AND cbgm.is_delete = false
        WHERE cbp.is_delete = false AND cbp.portal_id = :#{#cond.portalId}
            AND (coalesce(:#{#cond.searchKeyword}, null) is null or cbp.plain_content like :#{#cond.searchKeyword} or cbc.plain_content like :#{#cond.searchKeyword})
            AND ((cbp.author_id = :#{#cond.userId})
            OR (cbp.visibility = 'ALL_MEMBERS' AND cbp.scheduled_time < :#{#cond.currentTime})
            OR (cbp.visibility = 'GROUP' AND cbgm.user_id = :#{#cond.userId} AND cbp.scheduled_time <= :#{#cond.currentTime})
            OR (cbp.visibility = 'MY_CONNECTIONS'
                AND cbp.author_id in (SELECT f.followed_id FROM follow f WHERE f.follower_id = :#{#cond.userId} AND f.status = 'CONNECTED' AND f.is_delete = FALSE)
                AND cbp.scheduled_time < :#{#cond.currentTime}))
        group by cbp.id
        order by cbp.is_pin desc, cbp.scheduled_time desc""";

    @Query(value = queryGetPostIds, countQuery = "select count(1) from (" + queryGetPostIds + ") as temp", nativeQuery = true)
    Page<UUID> getPostIdsForDashboard(@Param("cond") RequestGetListCommunityBoardPost request, Pageable pageable);

    String queryGetPostIdsForMentionTab ="""
        SELECT cbp.id AS id
        FROM community_board_post cbp
        LEFT JOIN community_board_comment cbc on cbc.is_delete = false and cbc.post_id = cbp.id
        LEFT JOIN community_board_group_member cbgm ON cbgm.group_id = cbp.group_id AND cbgm.is_delete = false
        WHERE cbp.is_delete = false AND cbp.portal_id = :#{#cond.portalId}
            AND (coalesce(:#{#cond.searchKeyword}, null) is null or cbp.plain_content like :#{#cond.searchKeyword} or cbc.plain_content like :#{#cond.searchKeyword})
            AND (cbp.content like :#{#cond.mentionUser} or cbc.content like :#{#cond.mentionUser})
            AND ((cbp.author_id = :#{#cond.userId})
            OR (cbp.visibility = 'ALL_MEMBERS' AND cbp.scheduled_time < :#{#cond.currentTime})
            OR (cbp.visibility = 'GROUP' AND cbgm.user_id = :#{#cond.userId} AND cbp.scheduled_time <= :#{#cond.currentTime})
            OR (cbp.visibility = 'MY_CONNECTIONS'
                AND cbp.author_id in (SELECT f.followed_id FROM follow f WHERE f.follower_id = :#{#cond.userId} AND f.status = 'CONNECTED' AND f.is_delete = FALSE)
                AND cbp.scheduled_time < :#{#cond.currentTime}))
        group by cbp.id
        order by cbp.is_pin desc, cbp.scheduled_time desc""";

    @Query(value = queryGetPostIdsForMentionTab, countQuery = "select count(1) from (" + queryGetPostIdsForMentionTab + ") as temp", nativeQuery = true)
    Page<UUID> getPostIdsForMentionTab(@Param("cond") RequestGetListCommunityBoardPost request, Pageable pageable);

    String queryGetPostIdsForFollowingTab ="""
        SELECT cbp.id AS id
        FROM community_board_post cbp
        LEFT JOIN community_board_comment cbc on cbc.is_delete = false and cbc.post_id = cbp.id
        LEFT JOIN community_board_group_member cbgm ON cbgm.group_id = cbp.group_id AND cbgm.is_delete = false
        WHERE cbp.is_delete = false AND cbp.portal_id = :#{#cond.portalId}
            AND (coalesce(:#{#cond.searchKeyword}, null) is null or cbp.plain_content like :#{#cond.searchKeyword} or cbc.plain_content like :#{#cond.searchKeyword})
            AND cbp.author_id in (SELECT f.followed_id FROM follow f WHERE f.follower_id = :#{#cond.userId} AND f.status = 'CONNECTED' AND f.is_delete = FALSE)
            AND ((cbp.visibility = 'ALL_MEMBERS') OR (cbp.visibility = 'MY_CONNECTIONS')
                OR (cbp.visibility = 'GROUP' AND cbgm.user_id = :#{#cond.userId}))
            AND cbp.scheduled_time < :#{#cond.currentTime}
        group by cbp.id
        order by cbp.is_pin desc, cbp.scheduled_time desc""";

    @Query(value = queryGetPostIdsForFollowingTab, countQuery = "select count(1) from (" + queryGetPostIdsForFollowingTab + ") as temp", nativeQuery = true)
    Page<UUID> getPostIdsForFollowingTab(@Param("cond") RequestGetListCommunityBoardPost request, Pageable pageable);

    @Query("""
        SELECT cbp.id
        FROM CommunityBoardPost cbp
        JOIN CommunityBoardGroupMember cbgm on cbgm.groupId = cbp.groupId and cbgm.userId = :#{#cond.userId} and cbgm.isDelete = false
        LEFT JOIN CommunityBoardComment cbc on cbc.postId = cbp.id and cbc.isDelete = false
        WHERE cbp.isDelete = false AND cbp.portalId = :#{#cond.portalId}
            AND (coalesce(:#{#cond.searchKeyword}, null) is null or cbp.plainContent like :#{#cond.searchKeyword} or cbc.plainContent like :#{#cond.searchKeyword})
            and cbp.groupId = :#{#cond.groupId} and cbp.visibility = 'GROUP'
            and (cbp.scheduledTime <= :#{#cond.currentTime} or cbp.authorId = :#{#cond.userId})
        group by cbp.id
        order by cbp.isPin, cbp.scheduledTime desc
    """)
    Page<UUID> getPostIdsForGroup(@Param("cond") RequestGetListCommunityBoardPost request, Pageable pageable);

    String queryGetDetailPostFromIds ="""
         SELECT cbp.id AS id, cbp.content AS content, cbp.visibility AS visibility, cbp.group_id AS groupId, cbp.is_pin AS isPin, cbp.scheduled_time AS scheduledTime, cbp.is_notify_all as isNotifyAll, cblike.like_icon as likeIcon,
             cbg.group_name as groupName, json_strip_nulls(json_build_object('id', u.id, 'avatar', u.image_url,'fullName', u.normalized_full_name)) as author,
             fa.filesJson as files, la.likesJson as likes, ur.id as restrictPostId, jua.authority_name as authorityName,
             json_strip_nulls(json_build_object('id', ca.id, 'parentId', ca.parentId, 'postId', ca.post_id, 'content', ca.content, 'likeId', ca.likeId, 'likeIcon', ca.likeIcon,
                'createAt', ca.createAt, 'totalLike', ca.totalLike, 'totalCommentChild', ca.totalCommentChild, 'author', ca.author, 'files', ca.files)) AS firstComment,
             COALESCE((SELECT count(cbc.id) FROM community_board_comment cbc WHERE cbc.post_id = cbp.id AND cbc.is_delete = false), 0) AS totalComment
         FROM community_board_post cbp
         JOIN jhi_user u ON u.id = cbp.author_id
         JOIN jhi_user_authority jua ON jua.user_id = u.id
         LEFT JOIN community_board_group cbg on cbg.id = cbp.group_id
         LEFT JOIN community_board_like cblike on cblike.entry_id = cbp.id and cblike.entry_type = 'POST' and cblike.author_id = :userId
         LEFT JOIN community_board_user_restriction ur on ur.user_id = cbp.author_id and ur.portal_id = cbp.portal_id and ur.is_delete = false
         LEFT JOIN (
            SELECT cbf.entry_id AS post_id,
                COALESCE(json_agg(json_build_object('id', cbf.id, 'url', cbf.path, 'mediaType', cbf.type, 'name', cbf.real_name))) AS filesJson
             FROM community_board_file cbf
             WHERE cbf.entry_id in :postIds and cbf.entry_type = 'POST' and cbf.is_delete = false
             GROUP BY cbf.entry_id ) as fa ON fa.post_id = cbp.id
         LEFT JOIN (
             SELECT cbl.entry_id AS post_id,
                COALESCE(json_agg(json_build_object('id', cbl.id, 'author', json_build_object('id', u.id, 'avatar', u.image_url,'fullName', u.normalized_full_name)))) AS likesJson
             FROM community_board_like cbl
             JOIN jhi_user u ON u.id = cbl.author_id
             WHERE cbl.entry_id in :postIds and cbl.entry_type = 'POST'
             GROUP BY cbl.entry_id ) as la ON la.post_id = cbp.id
         LEFT JOIN (
             SELECT DISTINCT ON (cbc.post_id)
                 cbc.post_id AS post_id,
                 cbc.id as id,
                 cbc.parent_id as parentId,
                 cbc.content as content,
                 cbc.created_date as createAt,
                 ccbl.id as likeId, ccbl.like_icon as likeIcon,
                 COALESCE((SELECT count(cbcc.id) FROM community_board_comment cbcc WHERE cbcc.parent_id = cbc.id AND cbcc.is_delete = false), 0) as totalCommentChild,
                 COALESCE((SELECT count(cbl.id) FROM community_board_like cbl WHERE cbl.entry_id = cbc.id AND cbl.entry_type = 'COMMENT'), 0) as totalLike,
                 json_strip_nulls(json_build_object('id', u.id, 'avatar', u.image_url,'fullName', u.normalized_full_name)) as author,
                 COALESCE(json_agg(json_build_object('id', cbf.id, 'url', cbf.path, 'mediaType', cbf.type, 'name', cbf.real_name)) FILTER (WHERE cbf.id IS NOT NULL), '[]') AS files
             FROM community_board_comment cbc
             JOIN jhi_user u ON u.id = cbc.author_id
             LEFT JOIN community_board_file cbf ON cbf.entry_id = cbc.id and cbf.entry_type = 'COMMENT' and cbf.is_delete = false
             LEFT JOIN community_board_like ccbl on ccbl.entry_id = cbc.id and ccbl.entry_type = 'COMMENT' and ccbl.author_id = :userId
             WHERE cbc.post_id in :postIds and cbc.is_delete = false and cbc.parent_id is null
             GROUP BY cbc.post_id, cbc.id, u.id, ccbl.id
             ORDER BY cbc.post_id, cbc.created_date desc) as ca ON ca.post_id = cbp.id
         WHERE cbp.is_delete = false and cbp.id in :postIds
         order by cbp.is_pin desc, cbp.scheduled_time desc""";

    @Query(value = queryGetDetailPostFromIds, nativeQuery = true)
    List<IResponseCommunityBoardPost> getDetailPostFromIds(List<UUID> postIds, UUID userId);

    Optional<CommunityBoardPost> findByIsPinAndPortalId(Boolean isPin, UUID portalId);

    @Query("""
        SELECT u
        FROM CommunityBoardPost cbp
        JOIN User u on u.id = cbp.authorId
        WHERE cbp.id = :id
    """)
    Optional<User> findAuthorUser(UUID id);

    @Query("""
        SELECT p
        FROM CommunityBoardPost p
        JOIN CommunityBoardComment c on c.postId = p.id
        WHERE c.id = :commentId
    """)
    Optional<CommunityBoardPost> findPostByCommentId(UUID commentId);
}
