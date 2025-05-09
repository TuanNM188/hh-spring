package com.formos.huub.domain.response.communityboardcomment;

import com.formos.huub.domain.response.communityboard.ResponseCommunityBoardFile;
import com.formos.huub.domain.response.communityboard.ResponseCommunityBoardUser;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseCommunityBoardComment {

    private UUID id;
    private UUID parentId;
    private UUID postId;
    private ResponseCommunityBoardUser author;
    private String content;
    private Instant createAt;
    private Integer totalCommentChild;
    private Integer totalLike;
    private UUID likeId;
    private String likeIcon;
    private List<ResponseCommunityBoardFile> files;
    private ResponseCommunityBoardComment firstComment;
}
