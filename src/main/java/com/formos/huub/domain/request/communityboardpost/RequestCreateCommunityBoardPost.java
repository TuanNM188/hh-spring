package com.formos.huub.domain.request.communityboardpost;

import com.formos.huub.framework.validation.constraints.UUIDCheck;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class RequestCreateCommunityBoardPost {

    private String content;

    private String visibility;

    @UUIDCheck
    private String groupId;

    @UUIDCheck
    private String portalId;

    private Instant scheduledTime;

    private String isNotifyAll;

    private List<String> portalIds;

    private Boolean isPin = false;

    private List<RequestCommunityBoardFile> files;

    private List<String> mentionUserIds;
}
