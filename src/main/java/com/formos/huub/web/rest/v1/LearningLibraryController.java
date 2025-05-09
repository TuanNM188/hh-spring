package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.enums.ResourceFileTypeEnum;
import com.formos.huub.domain.request.learninglibrary.*;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.ValidationUtils;
import com.formos.huub.framework.validation.constraints.FileMaxSizeCheck;
import com.formos.huub.framework.validation.constraints.FileRequireCheck;
import com.formos.huub.framework.validation.constraints.FileTypeCheck;
import com.formos.huub.service.learninglibrary.LearningLibraryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

import static com.formos.huub.framework.enums.FileTypeEnum.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/learning-libraries")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LearningLibraryController {

    ResponseSupport responseSupport;

    LearningLibraryService learningLibraryService;

    Validator validator;

    @PreAuthorize("hasPermission(null, 'SEARCH_LEARNING_LIBRARY')")
    @PostMapping("/search")
    public ResponseEntity<ResponseData> search(@RequestBody RequestSearchLearningLibrary request) {
        return responseSupport.success(ResponseData.builder().data(learningLibraryService.searchLearningLibraries(request)).build());
    }

    @PreAuthorize("hasPermission(null, 'SEARCH_LEARNING_LIBRARY_CARD_VIEW')")
    @PostMapping("/search/card-view")
    public ResponseEntity<ResponseData> searchCardView(@RequestBody RequestSearchLearningLibrary request) {
        return responseSupport.success(
            ResponseData.builder().data(learningLibraryService.searchLearningLibrariesCardView(request)).build()
        );
    }

    @PreAuthorize("hasPermission(null, 'BOOKMARK_LEARNING_LIBRARY')")
    @PostMapping("/bookmark")
    public ResponseEntity<ResponseData> bookmarkLearningLibrary(@RequestBody RequestBookmarkLearningLibrary request) {
        return responseSupport.success(ResponseData.builder().data(learningLibraryService.bookmarkLearningLibrary(request)).build());
    }

    @PreAuthorize(
        "hasPermission(null, 'GET_LEARNING_LIBRARY_DETAIL') or " +
        "(hasPermission(null, 'GET_LEARNING_LIBRARY_DETAIL_OWN') " +
        "and @ownerCheckSecurity.isLearningLibraryGetDetail(#id))"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ResponseData> getDetail(@PathVariable String id) {
        var response = learningLibraryService.getDetail(UUID.fromString(id));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PreAuthorize(
        "hasPermission(null, 'GET_LEARNING_LIBRARY_OVERVIEW_BY_ID') or " +
        "(hasPermission(null, 'GET_LEARNING_LIBRARY_OVERVIEW_BY_ID_OWN') " +
        "and @ownerCheckSecurity.isLearningLibraryGetDetail(#id))"
    )
    @GetMapping("/overview/{id}")
    public ResponseEntity<ResponseData> getOverview(@PathVariable String id) {
        var response = learningLibraryService.getOverview(UUID.fromString(id));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PreAuthorize(
        "hasPermission(null, 'GET_LEARNING_LIBRARY_DETAIL_LEARNING_BY_ID') or " +
        "(hasPermission(null, 'GET_LEARNING_LIBRARY_DETAIL_LEARNING_BY_ID_OWN') " +
        "and @ownerCheckSecurity.isLearningLibraryGetDetail(#id))"
    )
    @GetMapping("/detail-learning/{id}")
    public ResponseEntity<ResponseData> getDetailLearning(@PathVariable String id) {
        var response = learningLibraryService.getDetailCourse(UUID.fromString(id));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PreAuthorize("hasPermission(null, 'CREATE_LEARNING_LIBRARY')")
    @PostMapping
    public ResponseEntity<ResponseData> create(@RequestBody @Valid RequestCreateLearningLibrary request) {
        UUID learningLibraryId = learningLibraryService.createLearningLibrary(request);

        Set<ConstraintViolation<RequestCreateLearningLibrary>> violations = Set.of();
        if (Boolean.FALSE.equals(request.getIsDraft())) {
            violations = validator.validate(request, FinalCourse.class, Default.class);
        }
        if (!violations.isEmpty()) {
            return ResponseEntity.badRequest().body(ValidationUtils.buildValidationErrors(violations));
        }

        return responseSupport.success(ResponseData.builder().data(learningLibraryId).build());
    }

    @PreAuthorize(
        "hasPermission(null, 'UPDATE_LEARNING_LIBRARY_DETAIL_ALL') or " +
        "( hasPermission(null, 'UPDATE_LEARNING_LIBRARY_DETAIL_OWN') " +
        "and @ownerCheckSecurity.isLearningLibraryUpdate(#id, authentication) )"
    )
    @PatchMapping("/{id}")
    public ResponseEntity<ResponseData> update(@PathVariable String id, @RequestBody @Valid RequestUpdateLearningLibrary request) {
        Set<ConstraintViolation<RequestUpdateLearningLibrary>> violations = Set.of();
        if (Boolean.FALSE.equals(request.getIsDraft())) {
            violations = validator.validate(request, FinalCourse.class, Default.class);
        }
        if (!violations.isEmpty()) {
            return ResponseEntity.badRequest().body(ValidationUtils.buildValidationErrors(violations));
        }

        learningLibraryService.updateLearningLibrary(UUID.fromString(id), request);
        return responseSupport.success(ResponseData.builder().build());
    }

    @PreAuthorize("hasPermission(null, 'DELETE_LEARNING_LIBRARY')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData> delete(@PathVariable String id) {
        learningLibraryService.deleteLearningLibrary(UUID.fromString(id));
        return responseSupport.success(ResponseData.builder().build());
    }

    @PreAuthorize("hasPermission(null, 'UPLOAD_LEARNING_LIBRARY_RESOURCE_IMAGE')")
    @PostMapping(value = "/resource/image", consumes = { "multipart/form-data" })
    public ResponseEntity<ResponseData> uploadImage(
        @RequestPart(value = "file") @Valid @FileRequireCheck @FileMaxSizeCheck(max = 15) @FileTypeCheck(
            allowedFileTypes = { PNG, JPG, SVG, SVG, WEBP }
        ) MultipartFile file
    ) {
        var response = learningLibraryService.uploadResource(ResourceFileTypeEnum.IMAGE, file, true);
        ResponseData responseData = ResponseData.builder().data(response).build();
        return responseSupport.success(responseData);
    }

    @PreAuthorize("hasPermission(null, 'UPLOAD_LEARNING_LIBRARY_RESOURCE_FILE')")
    @PostMapping(value = "/resource/file", consumes = { "multipart/form-data" })
    public ResponseEntity<ResponseData> uploadFile(
        @RequestPart(value = "file") @Valid @FileRequireCheck @FileMaxSizeCheck(max = 15) MultipartFile file
    ) {
        var response = learningLibraryService.uploadResource(ResourceFileTypeEnum.FILES, file, false);
        ResponseData responseData = ResponseData.builder().data(response).build();
        return responseSupport.success(responseData);
    }

    @PreAuthorize("hasPermission(null, 'RATING_LEARNING_LIBRARY')")
    @PostMapping("/rating")
    public ResponseEntity<ResponseData> ratingLearningLibrary(@RequestBody RequestRatingLearningLibrary request) {
        learningLibraryService.ratingLearningLibrary(request);
        return responseSupport.success(ResponseData.builder().build());
    }

    @PostMapping(value = "/lesson-survey", consumes = { "multipart/form-data" })
    public ResponseEntity<ResponseData> submitSurveyForm(@RequestParam("learningLibraryId") String learningLibraryId,
                                                         @RequestParam("lessonId") String lessonId,
                                                         @RequestParam("surveyData") String surveyData,
                                                         @RequestParam("file") @Valid @FileRequireCheck MultipartFile file) {
        learningLibraryService.submitLessonFormSurvey(UUID.fromString(learningLibraryId), UUID.fromString(lessonId), surveyData, file);
        return responseSupport.success(ResponseData.builder().build());
    }

    @PreAuthorize("hasPermission(null, 'REGISTER_LEARNING_LIBRARY')")
    @PostMapping(value = "/registration", consumes = { "multipart/form-data" })
    public ResponseEntity<ResponseData> registerLearningLibrary(@RequestParam("learningLibraryId") String learningLibraryId,
                                                                @RequestParam("isReviewRequired") String isReviewRequired,
                                                                @RequestParam(value = "surveyData", required = false) String surveyData,
                                                                @RequestParam(value = "file", required = false) MultipartFile file,
                                                                HttpServletRequest httpServletRequest) {
        String timezone = httpServletRequest.getHeader("X-Timezone");
        learningLibraryService.registerLearningLibrary(UUID.fromString(learningLibraryId),isReviewRequired, surveyData, file, timezone);
        return responseSupport.success(ResponseData.builder().build());
    }

    @PostMapping("/lesson-status")
    public ResponseEntity<ResponseData> updateLessonStatus(@RequestBody RequestUpdateLessonStatus request) {
        learningLibraryService.updateLessonStatus(request);
        return responseSupport.success(ResponseData.builder().build());
    }
}
