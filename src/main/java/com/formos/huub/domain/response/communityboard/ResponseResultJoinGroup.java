package com.formos.huub.domain.response.communityboard;

import com.formos.huub.domain.enums.CommunityBoardGroupRoleEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseResultJoinGroup {

    private UUID id;

    private UUID groupId;

    private UUID userId;

    private CommunityBoardGroupRoleEnum groupRole;

}
