package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.survey.*;
import com.formos.huub.domain.response.survey.ResponseSurvey;
import com.formos.huub.framework.enums.DateTimeFormat;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.DateUtils;
import com.formos.huub.framework.validation.constraints.FileRequireCheck;
import com.formos.huub.service.survey.SurveyResponsesService;
import com.formos.huub.service.survey.SurveyService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/survey")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SurveyController {

    ResponseSupport responseSupport;

    SurveyService surveyService;

    SurveyResponsesService surveyResponsesService;

    @PreAuthorize("hasPermission(null, 'SEARCH_SURVEY_LIST')")
    @PostMapping("/search")
    public ResponseEntity<ResponseData> search(@RequestBody RequestSearchSurvey request) {
        return responseSupport.success(ResponseData.builder().data(surveyService.searchSurvey(request)).build());
    }

    @PreAuthorize("hasPermission(null, 'CREATE_SURVEY')")
    @PostMapping
    public ResponseEntity<ResponseData> create(@RequestBody @Valid RequestCreateSurvey request) {
        UUID surveyId = surveyService.createSurvey(request);
        return responseSupport.success(ResponseData.builder().data(surveyId).build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'GET_DETAIL_SURVEY')")
    public ResponseEntity<ResponseData> getDetail(@PathVariable String id) {
        var response = surveyService.getDetail(UUID.fromString(id));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/browser/{id}")
    @PreAuthorize("@ownerCheckSecurity.isResponseSurveyForm(#id)")
    public ResponseEntity<ResponseData> getDetailForBrowser(@PathVariable String id) {
        var response = surveyService.getDetail(UUID.fromString(id));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PreAuthorize("hasPermission(null, 'UPDATE_SURVEY_DETAIL')")
    @PatchMapping("/{id}")
    public ResponseEntity<ResponseData> update(@PathVariable String id, @RequestBody @Valid RequestUpdateSurvey request) {
        surveyService.updateSurvey(UUID.fromString(id), request);
        return responseSupport.success(ResponseData.builder().build());
    }

    @GetMapping("/exists-name")
    public ResponseEntity<ResponseData> checkExistName(@RequestParam String name, @RequestParam(required = false) String surveyId) {
        var surveyIdUId = Optional.ofNullable(surveyId).map(UUID::fromString).orElse(null);
        return responseSupport.success(ResponseData.builder().data(surveyService.checkExistSurveyName(name, surveyIdUId)).build());
    }

    @PreAuthorize("hasPermission(null, 'DUPLICATE_SURVEY')")
    @PostMapping("/duplicate")
    public ResponseEntity<ResponseData> duplicate(@RequestBody @Valid RequestDuplicateSurvey request) {
        UUID surveyId = surveyService.duplicateSurvey(request);
        return responseSupport.success(ResponseData.builder().data(surveyId).build());
    }

    @PreAuthorize("hasPermission(null, 'UPDATE_SURVEY_DETAIL')")
    @PatchMapping("/update-status")
    public ResponseEntity<ResponseData> updateStatus(@RequestBody @Valid RequestUpdateStatusSurvey request) {
        var response = surveyService.updateStatus(request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PreAuthorize("hasPermission(null, 'DOWNLOAD_SURVEY_RESPONSES')")
    @GetMapping("/download-survey-responses/{id}")
    public ResponseEntity<byte[]> downloadSurveyResponses(@PathVariable String id) {
        ResponseSurvey survey = surveyService.getDetail(UUID.fromString(id));
        var response = surveyService.downloadSurveyResponses(UUID.fromString(id));
        HttpHeaders headers = new HttpHeaders();
        String formattedDate = DateUtils.convertInstantToStringTime(Instant.now(), DateTimeFormat.MMDDYYYY);
        headers.add(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=Survey Responses - " + survey.getName() + " " + formattedDate + ".csv"
        );
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
        return new ResponseEntity<>(response.toByteArray(), headers, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(null, 'SEARCH_SURVEY_RESPONSES_LIST')")
    @PostMapping("/{id}/responses/search")
    public ResponseEntity<ResponseData> searchSurveyResponses(@PathVariable String id, @RequestBody RequestSearchSurveyResponses request) {
        return responseSupport.success(
            ResponseData.builder().data(surveyResponsesService.searchSurveyResponses(UUID.fromString(id), request)).build()
        );
    }

    @PostMapping(value = "/submit-survey", consumes = { "multipart/form-data" })
    @PreAuthorize("@ownerCheckSecurity.isResponseSurveyForm(#surveyId)")
    public ResponseEntity<ResponseData> submitSurveyForm(
        @RequestParam("surveyId") String surveyId,
        @RequestParam("surveyData") String surveyData,
        @RequestParam("file") @Valid @FileRequireCheck MultipartFile file
    ) {
        surveyResponsesService.submitFormSurvey(UUID.fromString(surveyId), surveyData, file);
        return responseSupport.success(ResponseData.builder().build());
    }
}
