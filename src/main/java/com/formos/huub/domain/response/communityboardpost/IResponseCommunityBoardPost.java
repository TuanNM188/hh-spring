package com.formos.huub.domain.response.communityboardpost;

import com.formos.huub.domain.enums.CommunityBoardVisibilityEnum;
import java.time.Instant;
import java.util.UUID;

public interface IResponseCommunityBoardPost {
    UUID getId();
    String getAuthor();

    String getContent();
    String getGroupName();
    String getAuthorityName();
    CommunityBoardVisibilityEnum getVisibility();
    UUID getGroupId();
    UUID getRestrictPostId();
    String getLikeIcon();
    Boolean getIsPin();
    Boolean getIsNotifyAll();
    Instant getScheduledTime();
    Integer getTotalComment();

    String getFirstComment();
    String getLikes();
    String getFiles();
}
