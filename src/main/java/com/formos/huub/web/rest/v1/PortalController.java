package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.enums.FeatureCodeEnum;
import com.formos.huub.domain.request.portals.*;
import com.formos.huub.domain.response.portals.ResponsePortalIntakeQuestion;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import com.formos.huub.service.directmessage.DirectMessageService;
import com.formos.huub.service.feature.FeatureService;
import com.formos.huub.service.fundingservice.PortalFundingService;
import com.formos.huub.service.portals.PortalFormService;
import com.formos.huub.service.portals.PortalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/portals")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PortalController {

    ResponseSupport responseSupport;

    PortalService portalService;

    FeatureService featureService;

    PortalFundingService portalFundingService;

    PortalFormService portalFormService;

    DirectMessageService directMessageService;

    @PostMapping("/search")
    @PreAuthorize("hasPermission(null, 'SEARCH_PORTAL_LIST')")
    public ResponseEntity<ResponseData> searchPortals(@RequestBody RequestSearchPortals request) {
        return responseSupport.success(ResponseData.builder().data(portalService.searchPortals(request)).build());
    }

    @PostMapping("/funding/search")
    @PreAuthorize("hasPermission(null, 'SEARCH_PORTALS_FUNDING_LIST')")
    public ResponseEntity<ResponseData> searchPortalFunding(@RequestBody @Valid RequestSearchPortalFunding request) {
        return responseSupport.success(ResponseData.builder().data(portalFundingService.searchPortalFunding(request)).build());
    }

    @GetMapping("/exists-platform-name")
    public ResponseEntity<ResponseData> checkExistPlatformName(String platformName, String portalId) {
        var portalIdUId = Optional.ofNullable(portalId).map(UUID::fromString).orElse(null);
        return responseSupport.success(ResponseData.builder().data(portalService.checkExistPlatformName(platformName, portalIdUId)).build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'DELETE_PORTAL')")
    public ResponseEntity<ResponseData> deletePortals(@PathVariable @UUIDCheck String id) {
        portalService.deletePortal(UUID.fromString(id));
        return responseSupport.success();
    }

    @GetMapping("/program-managers")
    @PreAuthorize("hasPermission(null, 'GET_ALL_PROGRAM_MANAERS')")
    public ResponseEntity<ResponseData> getAllProgramManager(String portalId) {
        var response = portalService.getAllManager(UUIDUtils.toUUID(portalId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'CREATE_PORTAL')")
    public ResponseEntity<ResponseData> createPortal(@RequestBody @Valid RequestCreatePortal request, HttpServletRequest httpServletRequest) {
        String origin = httpServletRequest.getHeader("X-Subdomain");
        UUID id = portalService.createPortal(request, origin);
        return responseSupport.success(ResponseData.builder().data(id).build());
    }

    @GetMapping("/{id}")
    @PreAuthorize(
        "hasPermission(null, 'GET_PORTAL_DETAIL') or " +
            "(hasPermission(null, 'GET_PORTAL_DETAIL_OWN') " +
            "and @ownerCheckSecurity.isPortalUpdate(#id))"
    )
    public ResponseEntity<ResponseData> findDetailById(@PathVariable @UUIDCheck String id) {
        ResponseData result = ResponseData.builder().data(portalService.findDetailById(UUID.fromString(id))).build();
        return responseSupport.success(result);
    }

    @GetMapping("/{id}/about")
    @PreAuthorize(
        "hasPermission(null, 'GET_ABOUT_PORTAL')"
    )
    public ResponseEntity<ResponseData> getPortalAbout(@PathVariable @UUIDCheck String id) {
        ResponseData result = ResponseData.builder().data(portalService.getAboutPortal(UUID.fromString(id))).build();
        return responseSupport.success(result);
    }

    @GetMapping("/{id}/address")
    @PreAuthorize("hasPermission(null, 'GET_ADDRESS_PORTAL')")
    public ResponseEntity<ResponseData> getPortalAddress(@PathVariable @UUIDCheck String id) {
        ResponseData result = ResponseData.builder().data(portalService.getAddressPortal(UUID.fromString(id))).build();
        return responseSupport.success(result);
    }

    @PatchMapping("/{id}")
    @PreAuthorize(
        "hasPermission(null, 'UPDATE_PORTAL_DETAIL') or " +
            "(hasPermission(null, 'UPDATE_PORTAL_DETAIL_OWN') " +
            "and @ownerCheckSecurity.isPortalUpdate(#id))"
    )
    public ResponseEntity<ResponseData> updatePortal(@PathVariable @UUIDCheck String id, @RequestBody RequestUpdatePortal request,
                                                     HttpServletRequest httpServletRequest) {
        String origin = httpServletRequest.getHeader("X-Subdomain");
        portalService.updatePortal(UUID.fromString(id), request, origin);
        return responseSupport.success();
    }

    @GetMapping("/navigation-feature")
    @PreAuthorize("hasPermission(null, 'GET_PORTAL_NAVIGATION_FEATURE')")
    public ResponseEntity<ResponseData> getFeatureNavigation(@RequestParam FeatureCodeEnum featureCode, String portalId) {
        ResponseData result = ResponseData.builder().data(featureService.getFeatureNavigation(featureCode, portalId)).build();
        return responseSupport.success(result);
    }

    @GetMapping("/setting-feature")
    @PreAuthorize("hasPermission(null, 'GET_PORTAL_SETTING_FEATURE')")
    public ResponseEntity<ResponseData> getAllFeaturePortalSetting() {
        ResponseData result = ResponseData.builder().data(featureService.getAllFeaturePortalSetting()).build();
        return responseSupport.success(result);
    }

    @GetMapping("/meta")
    @PreAuthorize("hasPermission(null, 'GET_PORTAL_META')")
    public ResponseEntity<ResponseData> getAllPortalMeta() {
        return responseSupport.success(ResponseData.builder().data(portalService.getAllMeta()).build());
    }

    @GetMapping("/portal-host/exist-email")
    @PreAuthorize("hasPermission(null, 'CHECK_EXIST_PORTAL_PORTAL_HOST_EMAIL')")
    public ResponseEntity<ResponseData> existEmailPortalHost(@RequestParam String email) {
        ResponseData result = ResponseData.builder().data(portalService.existPortalHost(email)).build();
        return responseSupport.success(result);
    }

    @PostMapping("/portal-host/invite")
    @PreAuthorize("hasPermission(null, 'INVITE_PORTAL_PORTAL_HOST')")
    public ResponseEntity<ResponseData> invitePortalHost(@RequestBody @Valid RequestInvitePortalHost request) {
        var response = portalService.invitePortalHost(request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PostMapping("/portal-host/{portalHostId}/resend-invite")
    @PreAuthorize("hasPermission(null, 'RESEND_INVITE_PORTAL_PORTAL_HOST')")
    public ResponseEntity<ResponseData> resendInvitePortalHost(@PathVariable("portalHostId") String portalHostId, @RequestBody RequestInvitePortalHost request) {
        request.setPortalHostId(UUID.fromString(portalHostId));
        portalService.invitePortalHost(request);
        return responseSupport.success(ResponseData.builder().build());
    }

    @GetMapping("/{portalId}/intake-questions")
    @PreAuthorize("hasPermission(null, 'GET_PORTAL_INTAKE_QUESTIONS')")
    public ResponseEntity<ResponseData> getAllPortalQuestionByFormCode(@PathVariable @UUIDCheck String portalId) {
        ResponsePortalIntakeQuestion response = portalFormService.getAllPortalQuestion(UUID.fromString(portalId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PatchMapping("/answer-intake-questions")
    @PreAuthorize("hasPermission(null, 'ANSWER_PORTAL_INTAKE_QUESTIONS')")
    public ResponseEntity<ResponseData> fillIntakeQuestionsFormByPortal(@RequestBody @Valid RequestPortalIntakeQuestion request) {
        ResponsePortalIntakeQuestion response = portalFormService.fillIntakeQuestionsFormByPortal(request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/get-portal-info")
    public ResponseEntity<ResponseData> getPortalInfo() {
        var portalContext = PortalContextHolder.getContext();
        ResponseData result = ResponseData.builder().data(portalContext).build();
        return responseSupport.success(result);
    }

    @PatchMapping("/{id}/toggle-feature")
    @PreAuthorize(
        "hasPermission(null, 'UPDATE_PORTAL_DETAIL') or " +
            "(hasPermission(null, 'UPDATE_PORTAL_DETAIL_OWN') " +
            "and @ownerCheckSecurity.isPortalUpdate(#id))"
    )
    public ResponseEntity<ResponseData> updateToggleFeature(@PathVariable @UUIDCheck String id, @RequestBody RequestToggleFeature request) {
        portalService.updateToggleFeature(UUID.fromString(id), request);
        return responseSupport.success();
    }

    @GetMapping("/{portalId}/apply-1-1-support-screen")
    @PreAuthorize("hasPermission(null, 'GET_APPLY_1_1_SUPPORT_SCREEN_CONFIGURATIONS')")
    public ResponseEntity<ResponseData> answerAboutScreenConfigurationByPortal(@PathVariable String portalId) {
        var response = portalService.getApply11SupportScreenConfigurations(UUID.fromString(portalId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/{portalId}/apply-1-1-support-screen/apply-form")
    public ResponseEntity<ResponseData> getApplyTechnicalAssistanceApplicationForm(@PathVariable String portalId) {
        var response = portalFormService.getApplyTechnicalAssistanceForm(UUID.fromString(portalId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PostMapping("/{portalId}/apply-1-1-support-screen/apply")
    @PreAuthorize("hasPermission(null, 'SUBMIT_TECHNICAL_ASSISTANCE_APPLICATION_FORM')")
    public ResponseEntity<ResponseData> submitTechnicalAssistanceApplicationForm(@PathVariable String portalId,
                                                                                 @RequestBody RequestSubmitTechnicalAssistance request) {
        var response = portalFormService.userSubmitTechnicalAssistanceApplication(UUID.fromString(portalId), request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/{portalId}/check-exist-members")
    public ResponseEntity<ResponseData> checkExistMemberByPortalId(@PathVariable String portalId) {
        var response = directMessageService.checkMemberForPortal(portalId);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/welcome-message-sender")
    @PreAuthorize("hasPermission(null, 'GET_WELCOME_MESSAGE_SENDER')")
    public ResponseEntity<ResponseData> getWelcomeMessageSenders(@RequestParam String portalId) {
        ResponseData result = ResponseData.builder().data(portalService.getWelcomeMessageSenders(portalId)).build();
        return responseSupport.success(result);
    }

    @GetMapping("/for-business-owner")
    @PreAuthorize("hasPermission(null, 'GET_PORTAl_INFO_FOR_BUSINESS_OWNER')")
    public ResponseEntity<ResponseData> getPortalInfoForBusinessOwner(String portalId) {
        ResponseData result = ResponseData.builder().data(portalService.getInfoPortalForBusinessOwner(UUIDUtils.toUUID(portalId))).build();
        return responseSupport.success(result);
    }

    @GetMapping("/program/{programTermId}/is-exist-use")
    public ResponseEntity<ResponseData> checkExistUseProgramTerm(@PathVariable String programTermId) {
        ResponseData result = ResponseData.builder().data(portalService.isUseProgramTerm(UUIDUtils.toUUID(programTermId))).build();
        return responseSupport.success(result);
    }

    @GetMapping("/program/vendor/{vendorId}/is-exist-use")
    public ResponseEntity<ResponseData> checkExistUseProgramTermVendor(@PathVariable String vendorId) {
        ResponseData result = ResponseData.builder().data(portalService.isUseProgramTermVendor(UUIDUtils.toUUID(vendorId))).build();
        return responseSupport.success(result);
    }

    @GetMapping("/{portalId}/program-term")
    public ResponseEntity<ResponseData> getListProgramTermByPortal(@PathVariable String portalId) {
        ResponseData result = ResponseData.builder().data(portalService.getListProgramTermByPortal(UUIDUtils.toUUID(portalId))).build();
        return responseSupport.success(result);
    }

    @GetMapping("/{communityPartnerId}/community-partner")
    public ResponseEntity<ResponseData> getPortalByCommunityPartnerId(@PathVariable String communityPartnerId) {
        return responseSupport.success(ResponseData.builder().data(portalService.getPortalByCommunityPartnerId(UUIDUtils.toUUID(communityPartnerId))).build());

    }
}
