package com.formos.huub.domain.request.member;

import com.formos.huub.domain.enums.FollowStatusEnum;
import com.formos.huub.domain.enums.ViewTypeEnum;
import com.formos.huub.domain.request.common.SearchConditionRequest;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestSearchGroupMember extends SearchConditionRequest {

    private String searchKeyword;

    private String role;

    private UUID groupId;

    private UUID portalId;

    private UUID currentUserId;

    private List<String> excludeRoles;

    private FollowStatusEnum followedStatus;

    private FollowStatusEnum followerStatus;

    private UUID userId;

    private UUID communityPartnerId;
}
