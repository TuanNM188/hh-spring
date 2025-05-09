package com.formos.huub.domain.response.communityboard;

import com.formos.huub.domain.enums.CommunityBoardFileTypeEnum;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseCommunityBoardFile {

    private UUID id;
    private String url;
    private String name;
    private CommunityBoardFileTypeEnum mediaType;
}
