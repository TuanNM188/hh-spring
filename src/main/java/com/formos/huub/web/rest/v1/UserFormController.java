package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.useranswerform.RequestAnswerDemoGraphics;
import com.formos.huub.domain.request.useranswerform.RequestUserAnswerForm;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import com.formos.huub.service.useranswerform.UserFormService;
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
@RequestMapping("/forms")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserFormController {

    ResponseSupport responseSupport;

    UserFormService userFormService;

    @GetMapping("/question-by-form")
    @PreAuthorize("hasPermission(null, 'GET_FORM_QUESTION_BY_FORM')")
    public ResponseEntity<ResponseData> getAllQuestionByForm(@RequestParam String formCode) {
        var response = userFormService.getAllAnswerByForm(formCode);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/{userId}/answer-demographics")
    @PreAuthorize("hasPermission(null, 'GET_FORM_QUESTION_BY_FORM_ANSWER_DEMOGRAPHICS_BY_USER_ID')")
    public ResponseEntity<ResponseData> answerDemographic(@PathVariable @UUIDCheck String userId) {
        var response = userFormService.getAnswerDemographicsByUser(UUID.fromString(userId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/{userId}/profile-completion")
    public ResponseEntity<ResponseData> getProfileCompletionForTechnicalAdvisor(@PathVariable @UUIDCheck String userId) {
        var completedProfile = userFormService.getProfileCompletionForTechnicalAdvisor(UUID.fromString(userId));
        return responseSupport.success(ResponseData.builder().data(completedProfile).build());
    }

    @GetMapping("/{portalId}/about-screen-configuration/answer")
    @PreAuthorize("hasPermission(null, 'GET_FORM_QUESTION_BY_FORM_ABOUT_SCREEN_CONFIGURATION_BY_PORTAL_ID')")
    public ResponseEntity<ResponseData> answerAboutScreenConfigurationByPortal(@PathVariable @UUIDCheck String portalId) {
        var response = userFormService.getAnswerAboutScreenConfigurationByPortal(UUID.fromString(portalId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PostMapping("/{portalId}/about-screen-configuration/answer")
    @PreAuthorize("hasPermission(null, 'FILL_FORM_QUESTION_BY_FORM_ABOUT_SCREEN_CONFIGURATION_BY_PORTAL_ID')")
    public ResponseEntity<ResponseData> FillAnswerAboutScreenConfigurationByPortal(@PathVariable @UUIDCheck String portalId,
                                                                                   @RequestBody @Valid RequestUserAnswerForm userAnswerForm) {
        userFormService.fillFormAboutScreenConfiguration(UUID.fromString(portalId), userAnswerForm);
        return responseSupport.success(ResponseData.builder().build());
    }

    @PostMapping("/answer-demographics")
    @PreAuthorize("hasPermission(null, 'FILL_FORM_ANSWER_DEMOGRAPHICS')")
    public ResponseEntity<ResponseData> fillDemographicFormByUser(@RequestBody @Valid RequestAnswerDemoGraphics request) {
        userFormService.fillFormDemographics(request);
        return responseSupport.success();
    }

    @GetMapping("/{userId}/business-owner-answer")
    @PreAuthorize("hasPermission(null, 'GET_MEMBER_BUSINESS_OWNER_ANSWER_BY_USER_ID')")
    public ResponseEntity<ResponseData> getMemberBusinessOwnerAnswerByUserId(@PathVariable @UUIDCheck String userId, @RequestParam String displayForm) {
        var response = userFormService.getAllBusinessOwnerAnswer(UUID.fromString(userId), displayForm);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/{portalId}/business-owner-question/initial")
    @PreAuthorize("hasPermission(null, 'GET_MEMBER_BUSINESS_OWNER_QUESTION_BY_PORTAL_ID')")
    public ResponseEntity<ResponseData> getMemberBusinessOwnerQuestionByPortalId(@PathVariable @UUIDCheck String portalId, @RequestParam String displayForm) {
        var response = userFormService.getAllBusinessOwnerQuestionByPortal(UUID.fromString(portalId), displayForm);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }


    @GetMapping("/{technicalAssistanceId}/technical-assistance-answer")
    @PreAuthorize("hasPermission(null, 'GET_ANSWER_TECHNICAL_ASSISTANCE_FOR_BUSINESS_OWNER')")
    public ResponseEntity<ResponseData> getTechnicalAssistanceAnswerByUserId(@PathVariable @UUIDCheck String technicalAssistanceId) {
        var response = userFormService.getAllTechnicalAssistanceAnswer(UUID.fromString(technicalAssistanceId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PatchMapping("/technical-assistance-form/{technicalAssistanceId}")
    @PreAuthorize("hasPermission(null, 'UPDATE_ANSWER_TECHNICAL_ASSISTANCE_FOR_BUSINESS_OWNER')")
    public ResponseEntity<ResponseData> updateUserAnswerApplyTechnicalAssistanceForm(@PathVariable @UUIDCheck String technicalAssistanceId,
                                                                          @RequestBody RequestUserAnswerForm request) {
        userFormService.fillTechnicalAssistanceFormBusinessOwner(UUIDUtils.toUUID(technicalAssistanceId), request.getAnswerForms());
        return responseSupport.success(ResponseData.builder().build());
    }
}
