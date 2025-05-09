package com.formos.huub.domain.response.communityboardcomment;

import java.time.Instant;
import java.util.UUID;

public interface IResponseCommunityBoardComment {
    UUID getId();
    UUID getParentId();
    UUID getPostId();
    UUID getLikeId();
    String getLikeIcon();

    String getContent();
    Instant getCreateAt();
    String getAuthor();
    String getFirstComment();
    Integer getTotalCommentChild();

    Integer getTotalLike();
    String getFiles();
}
