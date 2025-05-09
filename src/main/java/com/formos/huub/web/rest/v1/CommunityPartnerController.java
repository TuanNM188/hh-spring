package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.RequestSearchCommunityPartner;
import com.formos.huub.domain.request.communitypartner.RequestCreateCommunityPartner;
import com.formos.huub.domain.request.communitypartner.RequestUpdateCommunityPartner;
import com.formos.huub.domain.request.invitemember.RequestInviteMember;
import com.formos.huub.domain.request.usersetting.RequestSearchAllCommunityPartner;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.communitypartner.CommunityPartnerService;
import com.formos.huub.service.invite.InviteService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community-partners")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommunityPartnerController {

    CommunityPartnerService communityPartnerService;

    InviteService inviteService;

    ResponseSupport responseSupport;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'GET_COMMUNITY_PARTNER_LIST')")
    public ResponseEntity<ResponseData> getAllCommunityPartners(RequestSearchAllCommunityPartner request) {
        return responseSupport.success(ResponseData.builder().data(communityPartnerService.getAllCommunityPartners(request)).build());
    }

    @PostMapping("/search")
    @PreAuthorize("hasPermission(null, 'SEARCH_COMMUNITY_PARTNER')")
    public ResponseEntity<ResponseData> searchCommunityPartners(@RequestBody RequestSearchCommunityPartner request) {
        return responseSupport.success(ResponseData.builder().data(communityPartnerService.searchCommunityPartners(request)).build());
    }

    @GetMapping("/{communityPartnerId}")
    @PreAuthorize("hasPermission(null, 'GET_COMMUNITY_PARTNER_BY_ID')")
    public ResponseEntity<ResponseData> getCommunityPartner(@PathVariable String communityPartnerId) {
        var response = communityPartnerService.getDetailCommunityPartner(UUID.fromString(communityPartnerId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'CREATE_COMMUNITY_PARTNER')")
    public ResponseEntity<ResponseData> createCommunityPartner(@RequestBody RequestCreateCommunityPartner request) {
        communityPartnerService.createCommunityPartner(request);
        return responseSupport.success(ResponseData.builder().build());
    }

    @PatchMapping("/{communityPartnerId}")
    @PreAuthorize("hasPermission(null, 'UPDATE_COMMUNITY_PARTNER')")
    public ResponseEntity<ResponseData> updateCommunityPartner(
        @RequestBody RequestUpdateCommunityPartner request,
        @PathVariable String communityPartnerId
    ) {
        communityPartnerService.updateCommunityPartner(UUID.fromString(communityPartnerId), request);
        return responseSupport.success(ResponseData.builder().build());
    }

    @PostMapping("/technical-advisor/invite")
    @PreAuthorize("hasPermission(null, 'COMMUNITY_PARTNER_INVITE_TECHNICAL_ADVISOR')")
    public ResponseEntity<ResponseData> inviteTechnicalAdvisor(@RequestBody RequestInviteMember request) {
        var response = inviteService.inviteTechnicalAdvisor(request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'DELETE_COMMUNITY_PARTNER')")
    public ResponseEntity<ResponseData> delete(@PathVariable String id) {
        communityPartnerService.deleteCommunityPartner(UUID.fromString(id));
        return responseSupport.success(ResponseData.builder().build());
    }

    @GetMapping("/meta")
    public ResponseEntity<ResponseData> getAllCommunityPartnerMeta(String portalId) {
        UUID portalIdReq = Objects.nonNull(portalId) ? UUID.fromString(portalId) : null;
        return responseSupport.success(ResponseData.builder().data(communityPartnerService.getAllCommunityPartnerMeta(portalIdReq)).build());
    }

    @GetMapping("/navigator/{communityPartnerId}")
    @PreAuthorize("hasPermission(null, 'GET_COMMUNITY_PARTNER_NAVIGATOR')")
    public ResponseEntity<ResponseData> getCommunityPartnerNavigator(@PathVariable String communityPartnerId, @RequestParam String userId) {
        var response = communityPartnerService.getCommunityPartnerNavigator(communityPartnerId, userId);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/technical-advisor/exists-email")
    public ResponseEntity<ResponseData> existsEmailTechnicalAdvisorStaff(@RequestParam String email) {
        ResponseData result = ResponseData.builder().data(communityPartnerService.getExistsTechnicalAdvisor(email)).build();
        return responseSupport.success(result);
    }

    @PostMapping("/{communityPartnerId}/staff/invite")
    @PreAuthorize("hasPermission(null, 'COMMUNITY_PARTNER_INVITE_STAFF')")
    public ResponseEntity<ResponseData> inviteCommunityPartnerStaff(@RequestBody RequestInviteMember request, @PathVariable String communityPartnerId) {
        var response = inviteService.inviteCommunityPartnerStaff(request, UUID.fromString(communityPartnerId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/staff/exists-email")
    @PreAuthorize("hasPermission(null, 'CHECK_EXISTS_COMMUNITY_PARTNER_STAFF_EMAIL')")
    public ResponseEntity<ResponseData> existsEmailCommunityPartnerStaff(@RequestParam String email) {
        ResponseData result = ResponseData.builder().data(inviteService.getExistsCommunityPartnerStaff(email)).build();
        return responseSupport.success(result);
    }

    @GetMapping("/portal/{portalId}")
    public ResponseEntity<ResponseData> getAllCommunityPartnerByPortalId(@PathVariable String portalId) {
        return responseSupport.success(ResponseData.builder().data(communityPartnerService.getAllCommunityPartnerByPortalId(portalId)).build());
    }

    @GetMapping("/primary-contact/{communityPartnerId}")
    @PreAuthorize("hasPermission(null, 'GET_COMMUNITY_PARTNER_NAVIGATOR')")
    public ResponseEntity<ResponseData> getCommunityPartnerPrimary(@PathVariable String communityPartnerId, @RequestParam String userId) {
        var response = communityPartnerService.getCommunityPartnerPrimary(communityPartnerId, userId);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

}
