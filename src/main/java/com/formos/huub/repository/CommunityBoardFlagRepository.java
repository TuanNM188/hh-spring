package com.formos.huub.repository;

import com.formos.huub.domain.entity.CommunityBoardFlag;
import com.formos.huub.domain.response.communityboardflag.IResponseCommunityBoardFlag;
import org.mapstruct.Mapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityBoardFlagRepository extends JpaRepository<CommunityBoardFlag, UUID> {

    @Query(value = """
        SELECT cbp.id AS id, cbp.content AS content, cbp.scheduled_time AS scheduledTime,cbp.visibility as visibility,cbp.group_id as groupId,cbp.id AS postId,
            reportUser.normalized_full_name AS reportBy, cbf.target_type AS moderateType, cbf.performed_at AS reportOn, cbf.reason AS reason,
            json_build_object('id', postAuthor.id, 'avatar', postAuthor.image_url,'fullName', postAuthor.normalized_full_name) as author,
            fa.filesJson as files, p.platform_name AS portalName, p.id AS portalId
        FROM community_board_flag cbf
        JOIN community_board_post cbp ON cbp.id = cbf.target_id AND cbf.target_type = 'POST' AND cbp.is_delete = false
        JOIN portal p ON cbf.portal_id = p.id
        LEFT JOIN jhi_user postAuthor ON postAuthor.id = cbp.author_id
        LEFT JOIN jhi_user reportUser ON reportUser.id = cbf.performed_id
        LEFT JOIN (
               SELECT cbfile.entry_id AS post_id,
                   COALESCE(json_agg(json_build_object('id', cbfile.id, 'url', cbfile.path, 'mediaType', cbfile.type, 'name', cbfile.real_name))) AS filesJson
                FROM community_board_file cbfile
                WHERE
                    cbfile.entry_id = :postId AND
                    cbfile.entry_type = 'POST' and cbfile.is_delete = FALSE
                    GROUP BY cbfile.entry_id) as fa
            ON fa.post_id = cbp.id
        WHERE cbf.is_delete = FALSE and cbf.id = :flagId
    """,  nativeQuery = true)
    Optional<IResponseCommunityBoardFlag> getDetailFlagPost(UUID flagId, UUID postId);

    @Query(value = """
        SELECT cbc.id AS id, cbc.content AS content, cbc.created_date AS scheduledTime,cbp.visibility as visibility,cbp.group_id as groupId,cbp.id AS postId,
            reportUser.normalized_full_name AS reportBy, cbf.target_type AS moderateType, cbf.performed_at AS reportOn, cbf.reason AS reason,
            json_build_object('id', cmtAuthor.id, 'avatar', cmtAuthor.image_url,'fullName', cmtAuthor.normalized_full_name) as author,
            fa.filesJson as files, p.platform_name AS portalName, p.id AS portalId
        FROM community_board_flag cbf
        JOIN community_board_comment cbc ON cbc.id = cbf.target_id AND cbf.target_type = 'COMMENT' AND cbc.is_delete = false
        JOIN community_board_post cbp ON cbp.id = cbc.post_id AND cbc.is_delete = false
        JOIN jhi_user cmtAuthor ON cmtAuthor.id = cbc.author_id
        JOIN jhi_user reportUser ON reportUser.id = cbf.performed_id
        JOIN portal p ON cbf.portal_id = p.id
        LEFT JOIN (
              SELECT cbfile.entry_id AS comment_id,
                  COALESCE(json_agg(json_build_object('id', cbfile.id, 'url', cbfile.path, 'mediaType', cbfile.type, 'name', cbfile.real_name))) AS filesJson
              FROM community_board_file cbfile
              WHERE cbfile.entry_id = :commentId AND cbfile.entry_type = 'COMMENT' and cbfile.is_delete = false
              GROUP BY cbfile.entry_id ) as fa
          ON fa.comment_id = cbc.id
        WHERE cbf.is_delete = FALSE and cbf.id = :flagId
    """,  nativeQuery = true)
    Optional<IResponseCommunityBoardFlag> getDetailFlagComment(UUID flagId, UUID commentId);
}
