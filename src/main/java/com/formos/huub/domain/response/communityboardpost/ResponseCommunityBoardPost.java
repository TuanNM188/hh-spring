package com.formos.huub.domain.response.communityboardpost;

import com.formos.huub.domain.enums.CommunityBoardVisibilityEnum;
import com.formos.huub.domain.response.communityboard.ResponseCommunityBoardFile;
import com.formos.huub.domain.response.communityboard.ResponseCommunityBoardUser;
import com.formos.huub.domain.response.communityboardcomment.ResponseCommunityBoardComment;
import com.formos.huub.domain.response.communityboardreaction.ResponseCommunityBoardReaction;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseCommunityBoardPost {

    private UUID id;
    private ResponseCommunityBoardUser author;
    private String content;
    private String groupName;
    private CommunityBoardVisibilityEnum visibility;
    private UUID groupId;
    private Boolean isPin;
    private Boolean isNotifyAll;
    private String likeIcon;
    private Instant scheduledTime;
    private Integer totalComment;

    private ResponseCommunityBoardComment firstComment;
    private List<ResponseCommunityBoardReaction> likes;
    private List<ResponseCommunityBoardFile> files;
}
