package com.formos.huub.domain.request.communityboard;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestCreateCommunityBoardGroup {

    private UUID portalId;

    @Valid
    private RequestCommunityBoardGroupDetail detail;

    @Valid
    private List<RequestCommunityBoardGroupSetting> settings;

    @Valid
    private RequestCommunityBoardGroupPhoto photo;
}
