package com.formos.huub.domain.response.communityboardpost;

import com.formos.huub.domain.enums.CommunityBoardVisibilityEnum;
import com.formos.huub.domain.enums.RoleEnum;
import com.formos.huub.domain.response.communityboardcomment.ResponseCommunityBoardCommentDetail;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResponseCommunityBoardPostDetail {

    UUID id;
    String author;
    String content;
    String groupName;
    CommunityBoardVisibilityEnum visibility;
    UUID groupId;
    Boolean isPin;
    Boolean isNotifyAll;
    String likeIcon;
    Instant scheduledTime;
    Integer totalComment;
    RoleEnum authorityName;
    UUID restrictPostId;

    ResponseCommunityBoardCommentDetail highlightComment;
    String firstComment;
    String likes;
    String files;
}
