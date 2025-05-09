package com.formos.huub.domain.response.communityboard;

import com.formos.huub.domain.enums.CommunityBoardFileTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public interface IResponseCommunityBoardFile {

    UUID getId();
    String getUrl();
    String getName();
    CommunityBoardFileTypeEnum getMediaType();
    Boolean getIsOwner();
}
