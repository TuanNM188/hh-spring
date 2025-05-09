package com.formos.huub.domain.response.communityboard;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseCommunityBoardUser {

    private UUID id;
    private String avatar;
    private String fullName;
}
