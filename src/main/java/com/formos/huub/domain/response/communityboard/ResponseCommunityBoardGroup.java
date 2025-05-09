package com.formos.huub.domain.response.communityboard;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResponseCommunityBoardGroup {

    private String id;

    private ResponseCommunityBoardGroupDetail detail;

    private List<ResponseCommunityBoardGroupSetting> settings;

    private ResponseCommunityBoardGroupPhoto photo;

}
