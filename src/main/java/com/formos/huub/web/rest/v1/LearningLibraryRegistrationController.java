package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.Learninglibraryregistration.RequestReviewRegistration;
import com.formos.huub.domain.request.Learninglibraryregistration.RequestSearchLearningLibraryRegistration;
import com.formos.huub.domain.request.Learninglibraryregistration.RequestSearchLessonSurvey;
import com.formos.huub.domain.response.learninglibraryregistration.ResponseDetailLessonSurvey;
import com.formos.huub.domain.response.learninglibraryregistration.ResponseDetailRegistration;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.HeaderUtils;
import com.formos.huub.service.learninglibraryregistration.LearningLibraryRegistrationService;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/learning-library-registrations")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LearningLibraryRegistrationController {

    LearningLibraryRegistrationService learningLibraryRegistrationService;

    ResponseSupport responseSupport;

    @PreAuthorize("hasPermission(null, 'REVIEW_LEARNING_LIBRARY_REGISTRATION')")
    @PostMapping("/review")
    public ResponseEntity<ResponseData> reviewCourseRegistration(@Valid @RequestBody RequestReviewRegistration request) {
        learningLibraryRegistrationService.reviewCourseRegistration(request);
        return responseSupport.success(ResponseData.builder().build());
    }

    @PostMapping("/search")
    @PreAuthorize("hasPermission(null, 'SEARCH_LEARNING_LIBRARY_REGISTRATION')")
    public ResponseEntity<ResponseData> searchRegistrationByTermAndCondition(
        @Valid @RequestBody RequestSearchLearningLibraryRegistration request, HttpServletRequest httpServletRequest) {
        request.setTimezone(HeaderUtils.getTimezone(httpServletRequest));
        return responseSupport.success(
            ResponseData.builder().data(learningLibraryRegistrationService.searchRegistrationByTermAndCondition(request)).build()
        );
    }

    @PostMapping("/search-lesson-survey")
    @PreAuthorize("hasPermission(null, 'SEARCH_COURSE_LESSON_SURVEY')")
    public ResponseEntity<ResponseData> searchLessonSurveyByTermAndCondition(
        @Valid @RequestBody RequestSearchLessonSurvey request , HttpServletRequest httpServletRequest) {
        request.setTimezone(HeaderUtils.getTimezone(httpServletRequest));
        return responseSupport.success(
            ResponseData.builder().data(learningLibraryRegistrationService.searchLessonSurveyByTermAndCondition(request)).build()
        );
    }

    @GetMapping("/{registrationId}")
    @PreAuthorize("hasPermission(null, 'GET_DETAIL_LEARNING_LIBRARY_REGISTRATION')")
    public ResponseEntity<ResponseData> getDetailRegistration(@PathVariable String registrationId) {
        ResponseDetailRegistration response = learningLibraryRegistrationService.getDetailRegistration(UUID.fromString(registrationId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/lesson-survey/{lessonSurveyId}")
    @PreAuthorize("hasPermission(null, 'GET_DETAIL_COURSE_LESSON_SURVEY')")
    public ResponseEntity<ResponseData> getDetailLessonSurvey(@PathVariable String lessonSurveyId) {
        ResponseDetailLessonSurvey response = learningLibraryRegistrationService.getDetailLessonSurvey(UUID.fromString(lessonSurveyId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

}
