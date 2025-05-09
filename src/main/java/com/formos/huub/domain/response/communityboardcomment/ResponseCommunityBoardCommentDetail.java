package com.formos.huub.domain.response.communityboardcomment;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResponseCommunityBoardCommentDetail {

    UUID id;
    UUID parentId;
    UUID postId;
    String author;
    String content;
    Instant createAt;
    Integer totalCommentChild;
    Integer totalLike;
    UUID likeId;
    String likeIcon;
    String files;
    ResponseCommunityBoardCommentDetail highlightComment;
    String firstComment;
}
