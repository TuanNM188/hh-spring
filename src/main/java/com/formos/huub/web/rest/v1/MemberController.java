package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.member.*;
import com.formos.huub.framework.base.BaseController;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import com.formos.huub.service.invite.InviteService;
import com.formos.huub.service.member.MemberService;
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
@RequestMapping("/members")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MemberController extends BaseController {

    ResponseSupport responseSupport;

    MemberService memberService;

    InviteService inviteService;

    @PostMapping("/search")
    @PreAuthorize("hasPermission(null, 'SEARCH_MEMBER_LIST')")
    public ResponseEntity<ResponseData> getAllMemberWithPageable(@Valid @RequestBody RequestSearchMember request) {
        return responseSupport.success(ResponseData.builder().data(memberService.getAllMemberWithPageable(request)).build());
    }

    @GetMapping("/{userId}/portals")
    public ResponseEntity<ResponseData> getAllPortalsByUser(@PathVariable String userId, boolean isAll) {
        var response = memberService.getPortalByUser(UUID.fromString(userId), isAll);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PostMapping("/follow")
    public ResponseEntity<ResponseData> userFollower(@Valid @RequestBody RequestFollowerUser request) {
        memberService.followUser(request);
        return responseSupport.success(ResponseData.builder().build());
    }


    @PostMapping("/generate-username")
    public ResponseEntity<ResponseData> generateUsername(@Valid @RequestBody RequestGenUsername request) {
        var response = inviteService.generateUsername(request.getFirstName(), request.getLastName());
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'CREATE_MEMBER')")
    public ResponseEntity<ResponseData> createMember(@RequestBody @Valid RequestMember request) {
        memberService.createMember(request);
        return responseSupport.success();
    }

    @GetMapping("/{memberId}")
    @PreAuthorize("hasPermission(null, 'GET_MEMBER_DETAIL')")
    public ResponseEntity<ResponseData> findMemberById(@PathVariable @UUIDCheck String memberId) {
        ResponseData result = ResponseData.builder().data(memberService.getMemberById(UUID.fromString(memberId))).build();
        return responseSupport.success(result);
    }

    @GetMapping("/public/{memberId}")
    public ResponseEntity<ResponseData> getPublicMemberById(@PathVariable @UUIDCheck String memberId) {
        ResponseData result = ResponseData.builder().data(memberService.getPublicDetailProfile(UUID.fromString(memberId))).build();
        return responseSupport.success(result);
    }

    @PostMapping("/public/{memberId}/connections")
    public ResponseEntity<ResponseData> getPublicMemberConnectionsById(@PathVariable @UUIDCheck String memberId,
                                                                       @RequestBody RequestSearchMemberConnection request) {
        var response = memberService.getAllMemberConnectionByUser(UUID.fromString(memberId), request);
        ResponseData result = ResponseData.builder().data(response).build();
        return responseSupport.success(result);
    }

    @PatchMapping("/{memberId}")
    @PreAuthorize("hasPermission(null, 'UPDATE_MEMBER_DETAIL')")
    public ResponseEntity<ResponseData> updateMember(@PathVariable @UUIDCheck String memberId, @RequestBody @Valid RequestMember request) {
        memberService.updateMember(UUID.fromString(memberId), request);
        return responseSupport.success();
    }

    @DeleteMapping("/{memberId}")
    @PreAuthorize("hasPermission(null, 'DELETE_MEMBER')")
    public ResponseEntity<ResponseData> deleteMember(@PathVariable @UUIDCheck String memberId) {
        memberService.deleteMember(UUID.fromString(memberId));
        return responseSupport.success();
    }
}
