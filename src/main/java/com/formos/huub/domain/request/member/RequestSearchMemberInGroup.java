package com.formos.huub.domain.request.member;

import com.formos.huub.domain.enums.CommunityBoardGroupRoleEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestSearchMemberInGroup extends RequestSearchMember {

    private CommunityBoardGroupRoleEnum groupRole;
    private String portalRole;

    private UUID groupId;
}
