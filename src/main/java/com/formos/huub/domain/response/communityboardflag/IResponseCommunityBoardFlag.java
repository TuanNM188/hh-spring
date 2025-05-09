package com.formos.huub.domain.response.communityboardflag;

import com.formos.huub.domain.enums.CommunityBoardVisibilityEnum;

import java.time.Instant;
import java.util.UUID;

public interface IResponseCommunityBoardFlag {
    UUID getId();
    UUID getPostId();
    String getReportBy();
    String getModerateType();
    Instant getReportOn();
    CommunityBoardVisibilityEnum getVisibility();
    UUID getGroupId();

    String getReason();
    String getAuthor();
    String getContent();
    UUID getPortalId();
    String getPortalName();
    Instant getScheduledTime();
    String getFiles();
}
