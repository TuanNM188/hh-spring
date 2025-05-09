package com.formos.huub.domain.request.communityboard;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class RequestCommunityBoardGroupInvite {

    private List<String> userIds;

    private String inviteMessage;

}
