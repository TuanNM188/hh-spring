package com.formos.huub.repository;

import com.formos.huub.domain.entity.CommunityBoardComment;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.response.communityboardcomment.IResponseCommunityBoardComment;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityBoardCommentRepository extends JpaRepository<CommunityBoardComment, UUID> {
    @Query("""
        SELECT cbc.id
        FROM CommunityBoardComment cbc
        WHERE cbc.postId = :postId
         and ((:parentId is null and cbc.parentId is null)
          or (:parentId is not null and cbc.parentId = :parentId))
         and (:ignoreCommentIds is null or cbc.id not in :ignoreCommentIds)
        ORDER BY cbc.createdDate desc""")
    Page<UUID> getCommentIdsForPost(UUID postId, UUID parentId, List<UUID> ignoreCommentIds, Pageable pageable);

    String queryGetDetailCommentFromIds = """
        SELECT cbc.id AS id, cbc.content AS content, cbc.created_date AS createAt, cbc.parent_id as parentId, cbc.post_id as postId, cblike.id as likeId, cblike.like_icon as likeIcon,
            json_strip_nulls(json_build_object('id', u.id, 'avatar', u.image_url,'fullName', u.normalized_full_name)) as author,
            fa.filesJson as files, la.likesJson as likes,
            json_strip_nulls(json_build_object('id', ca.id, 'postId', ca.postId, 'parentId', ca.parentId, 'content', ca.content, 'likeId', ca.likeId, 'likeIcon', ca.likeIcon,
                'createAt', ca.createAt, 'totalLike', ca.totalLike, 'totalCommentChild', ca.totalCommentChild, 'author', ca.author, 'files', ca.files)) AS firstComment,
            COALESCE((SELECT count(cbcc.id) FROM community_board_comment cbcc WHERE cbcc.parent_id = cbc.id AND cbcc.is_delete = false), 0) AS totalCommentChild,
            COALESCE((SELECT count(cbl.id) FROM community_board_like cbl WHERE cbl.entry_id = cbc.id AND cbl.entry_type = 'COMMENT'), 0) AS totalLike
        FROM community_board_comment cbc
        LEFT JOIN community_board_like cblike on cblike.entry_id = cbc.id and cblike.entry_type = 'COMMENT' and cblike.author_id = :userId
        JOIN jhi_user u ON u.id = cbc.author_id
        LEFT JOIN (
            SELECT cbf.entry_id AS comment_id,
                COALESCE(json_agg(json_build_object('id', cbf.id, 'url', cbf.path, 'mediaType', cbf.type, 'name', cbf.real_name))) AS filesJson
            FROM community_board_file cbf
            WHERE cbf.entry_id in :commentIds and cbf.entry_type = 'COMMENT' and cbf.is_delete = false
            GROUP BY cbf.entry_id ) as fa
        ON fa.comment_id = cbc.id
        LEFT JOIN (
            SELECT cbl.entry_id AS comment_id,
                COALESCE(json_agg(json_build_object('id', cbl.id, 'author', json_build_object('id', u.id, 'avatar', u.image_url,'fullName', u.normalized_full_name)))) AS likesJson
            FROM community_board_like cbl
            JOIN jhi_user u ON u.id = cbl.author_id
            WHERE cbl.entry_id in :commentIds and cbl.entry_type = 'COMMENT'
            GROUP BY cbl.entry_id ) as la
        ON la.comment_id = cbc.id
        LEFT JOIN (
             SELECT DISTINCT ON (cbc.parent_id)
                 cbc.parent_id AS parentId,
                 cbc.post_id as postId,
                 cbc.id as id,
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
             WHERE cbc.parent_id in :commentIds and cbc.is_delete = false
             GROUP BY cbc.parent_id, cbc.id, u.id, ccbl.id
             ORDER BY cbc.parent_id, cbc.created_date desc) as ca ON ca.parentId = cbc.id
        WHERE cbc.is_delete = false and cbc.id in :commentIds
        order by cbc.created_date desc""";

    @Query(value = queryGetDetailCommentFromIds, nativeQuery = true)
    List<IResponseCommunityBoardComment> getDetailCommentFromIds(List<UUID> commentIds, UUID userId);

    @Query("""
        SELECT u
        FROM CommunityBoardComment cbc
        JOIN User u on u.id = cbc.authorId
        WHERE cbc.id = :id
    """)
    Optional<User> findAuthorUser(UUID id);

    @Modifying
    @Query(value = """
        WITH RECURSIVE child_comments AS (
        SELECT cbc.id
        FROM community_board_comment cbc
        WHERE cbc.id = :commentId
        UNION ALL
        SELECT c.id
        FROM community_board_comment c
        INNER JOIN child_comments cc ON cc.id = c.parent_id)
        Update community_board_comment set is_delete = true WHERE id IN (SELECT id FROM child_comments)
    """, nativeQuery = true)
    void deleteCommentTree(UUID commentId);

    Integer countByPostId(UUID postId);
}
