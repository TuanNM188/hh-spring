package com.formos.huub.domain.response.communityboard;

import com.formos.huub.domain.enums.CommunityBoardGroupRoleEnum;
import com.formos.huub.domain.enums.CommunityBoardGroupStatusEnum;

import java.time.Instant;
import java.util.UUID;

public interface IResponseCommunityBoardGroup {

    UUID getId();
    String getGroupName();
    String getGroupAvatar();
    String getCoverPhoto();
    UUID getPortalId();
    Instant getLastActive();

    String getPrivacy();
    CommunityBoardGroupStatusEnum getStatus();

    CommunityBoardGroupRoleEnum getYourRole();

    String getMembers();
}
