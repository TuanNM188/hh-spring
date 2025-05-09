package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.businessowner.*;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.HeaderUtils;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.service.businessowner.BusinessOwnerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/business-owners")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BusinessOwnerController {

    ResponseSupport responseSupport;

    BusinessOwnerService businessOwnerService;

    @GetMapping("/{portalId}/register-form")
    public ResponseEntity<ResponseData> getRegisterForm(@PathVariable String portalId) {
        var response = businessOwnerService.getRegisterForm(UUID.fromString(portalId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/exist-email")
    public ResponseEntity<ResponseData> checkExistEmail(@RequestParam String email, @RequestParam(required = false) String userId) {
        var response = businessOwnerService.checkExistEmail(email, userId);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PostMapping("/{portalId}/register")
    public ResponseEntity<ResponseData> register(@PathVariable String portalId, @RequestBody @Valid RequestRegisterBusinessOwner request) {
        var response = businessOwnerService.registerBusinessOwner(UUID.fromString(portalId), request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PostMapping("/{userId}/sync-active-campaign-register")
    public ResponseEntity<ResponseData> handleActiveCampaignRegister(@PathVariable String userId) {
        businessOwnerService.handleActiveCampaignRegister(UUID.fromString(userId));
        return responseSupport.success();
    }

    @PreAuthorize("hasPermission(null, 'BUSINESS_OWNER_VISIT_PAGE')")
    @PostMapping("/visit-page")
    public ResponseEntity<ResponseData> visitPage(@RequestBody @Valid RequestBusinessOwnerVisitPage request) {
        businessOwnerService.handleActiveCampaignVisitPage(request.getFieldName());
        return responseSupport.success();
    }

    @PostMapping("/check-location-supported")
    public ResponseEntity<ResponseData> checkLocationSupported(@RequestBody @Valid RequestCheckPortalSupportedLocation request) {
        var response = businessOwnerService.checkLocationSupported(request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/register/transfer-data")
    public ResponseEntity<ResponseData> getRegisterDataByKey(@RequestParam String key) {
        var response = businessOwnerService.getRegisterDataByKey(key);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/portal/{portalId}")
    public ResponseEntity<ResponseData> getBusinessOwnerByPortalId(@PathVariable String portalId) {
        return responseSupport.success(ResponseData.builder().data(businessOwnerService.getBusinessOwnerByPortalId(portalId)).build());
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseData> getBusinessOwnerWithPageable(
        RequestSearchBusinessOwner request,
        @SortDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return responseSupport.success(
            ResponseData.builder().data(businessOwnerService.searchBusinessOwnerByConditions(request, pageable)).build()
        );
    }

    @GetMapping("/{userId}/info")
    public ResponseEntity<ResponseData> getBusinessOwnerInfo(@PathVariable String userId) {
        var response = businessOwnerService.getInfoBusinessOwner(UUIDUtils.toUUID(userId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PreAuthorize("hasPermission(null, 'SEARCH_EVENT_REGISTRATIONS')")
    @PostMapping("/{userId}/event-registrations/search")
    public ResponseEntity<ResponseData> searchEventRegistrations(
        @PathVariable String userId,
        @RequestBody RequestSearchEventRegistrations request
    ) {
        return responseSupport.success(ResponseData.builder().data(businessOwnerService.searchEventRegistrations(userId, request)).build());
    }

    @PreAuthorize("hasPermission(null, 'SEARCH_COURSE_REGISTRATIONS')")
    @PostMapping("/{userId}/course-registrations/search")
    public ResponseEntity<ResponseData> searchCourseRegistrations(
        @PathVariable String userId,
        @RequestBody RequestSearchCourseRegistrations request, HttpServletRequest httpServletRequest
    ) {
        request.setTimezone(HeaderUtils.getTimezone(httpServletRequest));
        return responseSupport.success(
            ResponseData.builder().data(businessOwnerService.searchCourseRegistrations(userId, request)).build()
        );
    }

    @PreAuthorize("hasPermission(null, 'SEARCH_COURSE_SURVEYS')")
    @PostMapping("/{userId}/course-surveys/search")
    public ResponseEntity<ResponseData> searchCourseSurveys(@PathVariable String userId, @RequestBody RequestSearchCourseSurveys request) {
        return responseSupport.success(ResponseData.builder().data(businessOwnerService.searchCourseSurveys(userId, request)).build());
    }

    @PreAuthorize("hasPermission(null, 'SEARCH_BO_SURVEYS')")
    @PostMapping("/{userId}/surveys/search")
    public ResponseEntity<ResponseData> searchBusinessOwnerSurveys(
        @PathVariable String userId,
        @RequestBody RequestSearchBusinessOwnerSurveys request
    ) {
        return responseSupport.success(
            ResponseData.builder().data(businessOwnerService.searchBusinessOwnerSurveys(userId, request)).build()
        );
    }
}
