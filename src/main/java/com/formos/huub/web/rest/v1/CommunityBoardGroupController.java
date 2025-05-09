package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.communityboard.*;
import com.formos.huub.domain.request.member.RequestSearchMemberInGroup;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import com.formos.huub.service.communityboard.CommunityBoardGroupService;
import com.formos.huub.service.member.MemberService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community-board-groups")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommunityBoardGroupController {

    ResponseSupport responseSupport;

    CommunityBoardGroupService communityBoardGroupService;
    MemberService memberService;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'CREATE_COMMUNITY_BOARD_GROUP')")
    public ResponseEntity<ResponseData> createCommunityBoardGroup(@RequestBody @Valid RequestCreateCommunityBoardGroup request) {
        UUID id = communityBoardGroupService.createCommunityBoardGroup(request);
        return responseSupport.success(ResponseData.builder().data(id).build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'GET_COMMUNITY_BOARD_GROUP_DETAIL')")
    public ResponseEntity<ResponseData> findDetailUpdateById(@PathVariable @UUIDCheck String id) {
        ResponseData result = ResponseData.builder().data(communityBoardGroupService.findDetailUpdateById(UUID.fromString(id))).build();
        return responseSupport.success(result);
    }

    @GetMapping("/all-group")
    @PreAuthorize("hasPermission(null, 'GET_COMMUNITY_BOARD_GROUP_DETAIL')")
    public ResponseEntity<ResponseData> findAllGroup(@Valid RequestGetCommunityBoardGroup request) {
        ResponseData result = ResponseData.builder().data(communityBoardGroupService.findAllGroup(request)).build();
        return responseSupport.success(result);
    }

    @GetMapping("/my-group")
    @PreAuthorize("hasPermission(null, 'GET_COMMUNITY_BOARD_GROUP_DETAIL')")
    public ResponseEntity<ResponseData> findMyGroup(@Valid RequestGetCommunityBoardGroup request) {
        ResponseData result = ResponseData.builder().data(communityBoardGroupService.findMyGroup(request)).build();
        return responseSupport.success(result);
    }

    @PostMapping("/members")
    @PreAuthorize("hasPermission(null, 'SEARCH_MEMBER_LIST')")
    public ResponseEntity<ResponseData> getAllMemberWithPageable(@Valid @RequestBody RequestSearchMemberInGroup request) {
        return responseSupport.success(ResponseData.builder().data(memberService.getAllMemberInGroupWithPageable(request)).build());
    }

    @PostMapping("/{id}/invite-members")
    @PreAuthorize("hasPermission(null, 'INVITE_MEMBER_TO_GROUP')")
    public ResponseEntity<ResponseData> inviteMembers(
        @PathVariable @UUIDCheck String id,
        @RequestBody @Valid RequestCommunityBoardGroupInvite request
    ) {
        communityBoardGroupService.saveListMemberInvite(UUIDUtils.convertToUUID(id), request);
        return responseSupport.success(ResponseData.builder().build());
    }

    @PatchMapping("/{id}/accept-invite-group")
    @PreAuthorize("hasPermission(null, 'ACCEPT_INVITE_TO_GROUP')")
    public ResponseEntity<ResponseData> acceptInviteGroup(@PathVariable @UUIDCheck String id) {
        communityBoardGroupService.acceptInviteGroup(UUIDUtils.convertToUUID(id));
        return responseSupport.success(ResponseData.builder().build());
    }

    @PatchMapping("/{id}/reject-invite-group")
    @PreAuthorize("hasPermission(null, 'REJECT_INVITE_TO_GROUP')")
    public ResponseEntity<ResponseData> rejectInviteGroup(@PathVariable @UUIDCheck String id) {
        communityBoardGroupService.rejectInviteGroup(UUIDUtils.convertToUUID(id));
        return responseSupport.success(ResponseData.builder().build());
    }

    @PostMapping("/{id}/join-group")
    @PreAuthorize("hasPermission(null, 'GET_COMMUNITY_BOARD_GROUP_DETAIL')")
    public ResponseEntity<ResponseData> actionJoinGroup(@PathVariable @UUIDCheck String id) {
        ResponseData result = ResponseData.builder().data(communityBoardGroupService.actionJoinGroup(UUIDUtils.convertToUUID(id))).build();
        return responseSupport.success(result);
    }

    @GetMapping("/detail/{id}")
    @PreAuthorize("hasPermission(null, 'GET_COMMUNITY_BOARD_GROUP_DETAIL')")
    public ResponseEntity<ResponseData> findDetailById(@PathVariable @UUIDCheck String id) {
        ResponseData result = ResponseData.builder().data(communityBoardGroupService.findDetailById(UUIDUtils.convertToUUID(id))).build();
        return responseSupport.success(result);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'UPDATE_COMMUNITY_BOARD_GROUP')")
    public ResponseEntity<ResponseData> updateCommunityBoardGroup(
        @PathVariable @UUIDCheck String id,
        @RequestBody RequestUpdateCommunityBoardGroup request
    ) {
        communityBoardGroupService.updateCommunityBoardGroup(UUIDUtils.convertToUUID(id), request);
        return responseSupport.success();
    }

    @GetMapping("/members")
    @PreAuthorize("hasPermission(null, 'GET_MEMBERS_INVITE_TO_GROUP')")
    public ResponseEntity<ResponseData> getListMemberInvite(@Valid RequestGetListMemberInvite request, Pageable pageable) {
        var result = communityBoardGroupService.getListMemberInvite(request, pageable);
        return responseSupport.success(ResponseData.builder().data(result).build());
    }

    @DeleteMapping("/{id}/member/{memberId}")
    @PreAuthorize("hasPermission(null, 'REMOVE_MEMBERS_IN_GROUP')")
    public ResponseEntity<ResponseData> removeMember(@PathVariable @UUIDCheck String id, @PathVariable @UUIDCheck String memberId) {
        communityBoardGroupService.removeMember(UUIDUtils.convertToUUID(id), UUIDUtils.convertToUUID(memberId));
        return responseSupport.success(ResponseData.builder().build());
    }

    @PatchMapping("/{id}/promote-to-moderator/{memberId}")
    @PreAuthorize("hasPermission(null, 'PROMOTE_MEMBERS_IN_GROUP')")
    public ResponseEntity<ResponseData> promoteModerator(@PathVariable @UUIDCheck String id, @PathVariable @UUIDCheck String memberId) {
        communityBoardGroupService.promoteModerator(UUIDUtils.convertToUUID(id), UUIDUtils.convertToUUID(memberId));
        return responseSupport.success(ResponseData.builder().build());
    }

    @PatchMapping("/{id}/promote-to-organizer/{memberId}")
    @PreAuthorize("hasPermission(null, 'PROMOTE_MEMBERS_IN_GROUP')")
    public ResponseEntity<ResponseData> promoteOrganizer(@PathVariable @UUIDCheck String id, @PathVariable @UUIDCheck String memberId) {
        communityBoardGroupService.promoteOrganizer(UUIDUtils.convertToUUID(id), UUIDUtils.convertToUUID(memberId));
        return responseSupport.success(ResponseData.builder().build());
    }


    @PatchMapping("/{id}/demote-to-member/{memberId}")
    @PreAuthorize("hasPermission(null, 'DEMOTE_MEMBERS_IN_GROUP')")
    public ResponseEntity<ResponseData> demoteModerator(@PathVariable @UUIDCheck String id, @PathVariable @UUIDCheck String memberId) {
        communityBoardGroupService.demoteToMember(UUIDUtils.convertToUUID(id), UUIDUtils.convertToUUID(memberId));
        return responseSupport.success(ResponseData.builder().build());
    }

    @GetMapping("/select-option")
    @PreAuthorize("hasPermission(null, 'GET_ALL_COMMUNITY_BOARD_GROUP')")
    public ResponseEntity<ResponseData> findAllGroupForMemberSelectOption(RequestListGroupSelectOption request) {
        var result = communityBoardGroupService.findAllGroupForMemberSelectOption(request);
        return responseSupport.success(ResponseData.builder().data(result).build());
    }

    @GetMapping("/initial-settings")
    @PreAuthorize("hasPermission(null, 'GET_COMMUNITY_BOARD_GROUP_INITIAL_SETTINGS')")
    public ResponseEntity<ResponseData> getCommunityBoardGroupInitialSettings() {
        var result = communityBoardGroupService.getCommunityBoardGroupInitialSettings();
        return responseSupport.success(ResponseData.builder().data(result).build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'DELETE_COMMUNITY_BOARD_GROUP')")
    public ResponseEntity<ResponseData> deleteCommunityBoardGroup(@PathVariable @UUIDCheck String id) {
        communityBoardGroupService.deleteCommunityBoardGroup(UUIDUtils.convertToUUID(id));
        return responseSupport.success(ResponseData.builder().build());
    }

    @PatchMapping("/{id}/leave-group")
    @PreAuthorize("hasPermission(null, 'GET_COMMUNITY_BOARD_GROUP_DETAIL')")
    public ResponseEntity<ResponseData> actionLeaveGroup(@PathVariable @UUIDCheck String id) {
        communityBoardGroupService.actionLeaveGroup(UUIDUtils.convertToUUID(id));
        return responseSupport.success(ResponseData.builder().build());
    }

    @GetMapping("/{id}/manager/request-join")
    @PreAuthorize("hasPermission(null, 'GET_ALL_COMMUNITY_BOARD_GROUP')")
    public ResponseEntity<ResponseData> getRequestJoinGroup(@PathVariable @UUIDCheck String id, Pageable pageable) {
        var result = communityBoardGroupService.getRequestJoinGroup(UUIDUtils.convertToUUID(id), pageable);
        return responseSupport.success(ResponseData.builder().data(result).build());
    }

    @GetMapping("/invited")
    public ResponseEntity<ResponseData> getInvitedGroupsByMember(@RequestParam(required = false) @UUIDCheck String userId, Pageable pageable) {
        var result = communityBoardGroupService.getInvitedGroups(UUIDUtils.toUUID(userId), pageable);
        return responseSupport.success(ResponseData.builder().data(result).build());
    }

    @PatchMapping("/{id}/manager/accept-join-group/{memberId}")
    @PreAuthorize("hasPermission(null, 'GET_COMMUNITY_BOARD_GROUP_DETAIL')")
    public ResponseEntity<ResponseData> acceptJoinGroupByManager(
        @PathVariable @UUIDCheck String id,
        @PathVariable @UUIDCheck String memberId
    ) {
        communityBoardGroupService.acceptJoinGroupByManager(UUIDUtils.convertToUUID(id), UUIDUtils.convertToUUID(memberId));
        return responseSupport.success(ResponseData.builder().build());
    }

    @PatchMapping("/{id}/manager/reject-join-group/{memberId}")
    @PreAuthorize("hasPermission(null, 'GET_COMMUNITY_BOARD_GROUP_DETAIL')")
    public ResponseEntity<ResponseData> rejectJoinGroupByManager(
        @PathVariable @UUIDCheck String id,
        @PathVariable @UUIDCheck String memberId
    ) {
        communityBoardGroupService.rejectJoinGroupByManager(UUIDUtils.convertToUUID(id), UUIDUtils.convertToUUID(memberId));
        return responseSupport.success(ResponseData.builder().build());
    }

}
