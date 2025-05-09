package com.formos.huub.domain.request.communityboardcomment;

import com.formos.huub.domain.request.communityboardpost.RequestCommunityBoardFile;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import java.util.List;
import java.util.UUID;

import com.formos.huub.framework.validation.constraints.UUIDCheck;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestCreateCommunityBoardComment {

    private String content;

    @UUIDCheck
    private String postId;

    @UUIDCheck
    private String parentId;

    private List<RequestCommunityBoardFile> files;
    private List<UUID> mentionUserIds;
}
