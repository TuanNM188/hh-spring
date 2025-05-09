package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.communityboardcomment.RequestCreateCommunityBoardComment;
import com.formos.huub.domain.request.communityboardcomment.RequestGetListCommunityBoardComment;
import com.formos.huub.domain.request.communityboardcomment.RequestUpdateCommunityBoardComment;
import com.formos.huub.domain.request.communityboardcomment.RequestGetListMemberMention;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import com.formos.huub.service.communityboard.CommunityBoardCommentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community-board-comment")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommunityBoardCommentController {

    ResponseSupport responseSupport;

    CommunityBoardCommentService communityBoardCommentService;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'CREATE_COMMUNITY_BOARD_COMMENT')")
    public ResponseEntity<ResponseData> createCommunityBoardComment(@RequestBody @Valid RequestCreateCommunityBoardComment request) {
        var comment = communityBoardCommentService.createComment(request);
        return responseSupport.success(ResponseData.builder().data(comment).build());
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'GET_COMMUNITY_BOARD_COMMENT')")
    public ResponseEntity<ResponseData> getAllCommunityResourceInPortal(RequestGetListCommunityBoardComment request, Pageable pageable) {
        var response = communityBoardCommentService.getCommentForPost(request, pageable);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PreAuthorize("hasPermission(null, 'UPDATE_COMMUNITY_BOARD_COMMENT')")
    @PatchMapping("/{commentId}")
    public ResponseEntity<ResponseData> updateComment(
        @PathVariable @UUIDCheck String commentId,
        @RequestBody @Valid RequestUpdateCommunityBoardComment request
    ) {
        communityBoardCommentService.updateComment(request, UUID.fromString(commentId));
        return responseSupport.success(ResponseData.builder().build());
    }

    @PreAuthorize("hasPermission(null, 'DELETE_COMMUNITY_BOARD_COMMENT')")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ResponseData> deleteComment(@PathVariable @UUIDCheck String commentId) {
        communityBoardCommentService.deleteComment(UUID.fromString(commentId));
        return responseSupport.success();
    }

    @GetMapping("/members")
    @PreAuthorize("hasPermission(null, 'CREATE_COMMUNITY_BOARD_COMMENT')")
    public ResponseEntity<ResponseData> getListMemberMention(RequestGetListMemberMention request) {
        var result = communityBoardCommentService.getListMemberMention(request);
        return responseSupport.success(ResponseData.builder().data(result).build());
    }
}
