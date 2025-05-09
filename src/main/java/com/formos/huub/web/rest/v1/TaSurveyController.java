package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.tasurvey.RequestSearchTaSurveyAppointment;
import com.formos.huub.domain.request.tasurvey.RequestSearchTaSurveyProject;
import com.formos.huub.domain.request.tasurvey.RequestSearchTaSurveyTap;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.HeaderUtils;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.service.tasurvey.TaSurveyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ta-surveys")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaSurveyController {

    ResponseSupport responseSupport;

    TaSurveyService taSurveyService;

    @PreAuthorize("hasPermission(null, 'TA_SURVEY_OVERVIEW')")
    @GetMapping("/overview")
    public ResponseEntity<ResponseData> getTaSurveyOverview(@RequestParam(required = false) String portalId) {
        return responseSupport.success(
            ResponseData.builder().data(taSurveyService.getTaSurveyOverview(UUIDUtils.toUUID(portalId))).build()
        );
    }

    @PreAuthorize("hasPermission(null, 'TA_SURVEY_OVERVIEW')")
    @PostMapping("/appointment")
    public ResponseEntity<ResponseData> getTaSurveyAppointment(@RequestBody RequestSearchTaSurveyAppointment request, HttpServletRequest httpServletRequest) {
        request.setTimezone(HeaderUtils.getTimezone(httpServletRequest));
        return responseSupport.success(
            ResponseData.builder().data(taSurveyService.getTaSurveyAppointment(request)).build()
        );
    }

    @PreAuthorize("hasPermission(null, 'TA_SURVEY_OVERVIEW')")
    @PostMapping("/project")
    public ResponseEntity<ResponseData> getTaSurveyProject(@RequestBody RequestSearchTaSurveyProject request) {
        return responseSupport.success(
            ResponseData.builder().data(taSurveyService.getTaSurveyProject(request)).build()
        );
    }

    @PreAuthorize("hasPermission(null, 'TA_SURVEY_OVERVIEW')")
    @PostMapping("/tap")
    public ResponseEntity<ResponseData> getTaSurveyTap(@RequestBody RequestSearchTaSurveyTap request) {
        return responseSupport.success(
            ResponseData.builder().data(taSurveyService.getTaSurveyTap(request)).build()
        );
    }

}
