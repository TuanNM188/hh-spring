package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.communityboardpost.*;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import com.formos.huub.service.communityboard.CommunityBoardPostService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community-board-post")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommunityBoardPostController {

    ResponseSupport responseSupport;

    CommunityBoardPostService communityBoardPostService;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'CREATE_COMMUNITY_BOARD_POST')")
    public ResponseEntity<ResponseData> createCommunityBoardPost(@RequestBody @Valid RequestCreateCommunityBoardPost request) {
        var post = communityBoardPostService.createPost(request);
        return responseSupport.success(ResponseData.builder().data(post).build());
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'GET_COMMUNITY_BOARD_POSTS')")
    public ResponseEntity<ResponseData> getPostForMember(
        RequestGetListCommunityBoardPost request,
        Pageable pageable
    ) {
        var response = communityBoardPostService.getPostForMember(request, pageable);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/mention")
    @PreAuthorize("hasPermission(null, 'GET_COMMUNITY_BOARD_POSTS')")
    public ResponseEntity<ResponseData> getPostsMentionTab(
        RequestGetListCommunityBoardPost request,
        Pageable pageable
    ) {
        var response = communityBoardPostService.getPostsMentionTab(request, pageable);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/following")
    @PreAuthorize("hasPermission(null, 'GET_COMMUNITY_BOARD_POSTS')")
    public ResponseEntity<ResponseData> getPostsFollowingTab(
        RequestGetListCommunityBoardPost request,
        Pageable pageable
    ) {
        var response = communityBoardPostService.getPostsFollowingTab(request, pageable);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PreAuthorize("hasPermission(null, 'GET_COMMUNITY_BOARD_POSTS')")
    @GetMapping("/{postId}")
    public ResponseEntity<ResponseData> getDetailPost(
            @PathVariable @UUIDCheck String postId,
            @RequestParam(required = false) String highlightCommentId
    ) {
        var response = communityBoardPostService.getDetailPost(UUIDUtils.convertToUUID(postId), highlightCommentId);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PreAuthorize("hasPermission(null, 'UPDATE_COMMUNITY_BOARD_POST')")
    @PatchMapping("/{postId}")
    public ResponseEntity<ResponseData> updatePost(
        @PathVariable @UUIDCheck String postId,
        @RequestBody @Valid RequestUpdateCommunityBoardPost request
    ) {
        var response = communityBoardPostService.updatePost(request, UUIDUtils.convertToUUID(postId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PreAuthorize("hasPermission(null, 'DELETE_COMMUNITY_BOARD_POST')")
    @DeleteMapping("/{postId}")
    public ResponseEntity<ResponseData> deletePost(@PathVariable @UUIDCheck String postId) {
        communityBoardPostService.deletePost(UUIDUtils.convertToUUID((postId)));
        return responseSupport.success();
    }

    @PostMapping("/{postId}/pin")
    @PreAuthorize("hasPermission(null, 'CREATE_COMMUNITY_BOARD_POST')")
    public ResponseEntity<ResponseData> createPinPost(@PathVariable @UUIDCheck String postId) {
        communityBoardPostService.createPinPost(UUIDUtils.convertToUUID((postId)));
        return responseSupport.success(ResponseData.builder().build());
    }

    @PostMapping("/{postId}/unpin")
    @PreAuthorize("hasPermission(null, 'CREATE_COMMUNITY_BOARD_POST')")
    public ResponseEntity<ResponseData> removePinPost(@PathVariable @UUIDCheck String postId) {
        communityBoardPostService.removePinPost(UUIDUtils.convertToUUID((postId)));
        return responseSupport.success(ResponseData.builder().build());
    }

    @PostMapping("/members")
    @PreAuthorize("hasPermission(null, 'CREATE_COMMUNITY_BOARD_POST')")
    public ResponseEntity<ResponseData> getListMemberMention(@RequestBody @Valid RequestGetListMemberMention request) {
        var result = communityBoardPostService.getListMemberMention(request);
        return responseSupport.success(ResponseData.builder().data(result).build());
    }

    @GetMapping("/{postId}/total-comment")
    @PreAuthorize("hasPermission(null, 'GET_COMMUNITY_BOARD_COMMENT')")
    public ResponseEntity<ResponseData> getListMemberMention(@PathVariable @UUIDCheck String postId) {
        var result = communityBoardPostService.getTotalComment(UUIDUtils.convertToUUID((postId)));
        return responseSupport.success(ResponseData.builder().data(result).build());
    }
}
