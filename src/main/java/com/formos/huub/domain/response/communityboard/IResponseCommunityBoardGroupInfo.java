package com.formos.huub.domain.response.communityboard;

import java.util.UUID;

public interface IResponseCommunityBoardGroupInfo {

    UUID getId();
    String getGroupName();
    String getGroupAvatar();
    String getCoverPhoto();
    UUID getPortalId();

    String getOrganizerUser();
}
