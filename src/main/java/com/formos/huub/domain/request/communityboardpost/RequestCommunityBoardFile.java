package com.formos.huub.domain.request.communityboardpost;

import com.formos.huub.domain.enums.CommunityBoardFileTypeEnum;
import com.formos.huub.framework.validation.constraints.EnumCheck;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RequestCommunityBoardFile {

    private String id;
    private String url;

    @EnumCheck(enumClass = CommunityBoardFileTypeEnum.class)
    private String mediaType;

    private String name;
}
