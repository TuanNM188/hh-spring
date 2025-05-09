package com.formos.huub.domain.response.communityboardreaction;

import com.formos.huub.domain.response.communityboard.ResponseCommunityBoardUser;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseCommunityBoardReaction {

    private UUID id;
    private ResponseCommunityBoardUser author;
}
