package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.communityboardlike.RequestAddLikeCommunityBoardComment;
import com.formos.huub.domain.request.communityboardlike.RequestAddLikeCommunityBoardPost;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import com.formos.huub.service.communityboard.CommunityBoardLikeService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community-board-reaction")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommunityBoardLikeController {

    ResponseSupport responseSupport;

    CommunityBoardLikeService communityBoardLikeService;

    @PostMapping("/post")
    @PreAuthorize("hasPermission(null, 'CREATE_COMMUNITY_BOARD_POST')")
    public ResponseEntity<ResponseData> addOrRemoveLikeToPost(@RequestBody @Valid RequestAddLikeCommunityBoardPost request) {
        var result = communityBoardLikeService.addOrRemoveLikeToPost(request);
        return responseSupport.success(ResponseData.builder().data(result).build());
    }

    @PostMapping("/comment")
    @PreAuthorize("hasPermission(null, 'CREATE_COMMUNITY_BOARD_POST')")
    public ResponseEntity<ResponseData> addOrRemoveLikeToComment(@RequestBody @Valid RequestAddLikeCommunityBoardComment request) {
        var result = communityBoardLikeService.addOrRemoveLikeToComment(request);
        return responseSupport.success(ResponseData.builder().data(result).build());
    }

    @GetMapping("/{entryId}")
    @PreAuthorize("hasPermission(null, 'CREATE_COMMUNITY_BOARD_POST')")
    public ResponseEntity<ResponseData> getListReaction(@PathVariable @UUIDCheck String entryId) {
        var result = communityBoardLikeService.getListReaction(UUIDUtils.convertToUUID(entryId));
        return responseSupport.success(ResponseData.builder().data(result).build());
    }
}
