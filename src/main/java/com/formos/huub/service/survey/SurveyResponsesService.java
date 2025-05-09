package com.formos.huub.service.survey;

import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.request.survey.*;
import com.formos.huub.domain.response.survey.ResponseSurveyResponses;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.*;
import com.formos.huub.mapper.survey.SurveyResponsesMapper;
import com.formos.huub.repository.SurveyRepository;
import com.formos.huub.repository.SurveyResponsesRepository;
import com.formos.huub.repository.UserRepository;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.file.FileService;
import java.time.Instant;
import java.util.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SurveyResponsesService {

    SurveyResponsesRepository surveyResponsesRepository;

    SurveyResponsesMapper surveyResponsesMapper;

    UserRepository userRepository;

    FileService fileService;

    SurveyRepository surveyRepository;

    private static final String SURVEY_FORM_RESOURCE_KEY = "surveyForm";

    /**
     * search Survey Responses
     *
     * @param request RequestSearchSurveyResponses
     * @return Map<String, Object>
     */
    public Map<String, Object> searchSurveyResponses(UUID surveyId, RequestSearchSurveyResponses request) {
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "submissionDate,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        var data = surveyResponsesRepository.searchSurveyResponsesByTermAndCondition(surveyId, request, pageable);
        return PageUtils.toPage(data);
    }

    /**
     * get Survey Responses entity
     *
     * @param surveyResponsesId UUID
     */
    private SurveyResponses getSurveyResponses(UUID surveyResponsesId) {
        return surveyResponsesRepository
            .findById(surveyResponsesId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Survey Responses")));
    }

    /**
     * get User Original Role
     *
     * @param user User
     */
    private String getUserOriginalRole(User user) {
        return user
            .getAuthorities()
            .stream()
            .findFirst()
            .map(Authority::getName)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Role")));
    }

    /**
     * get Survey Responses
     *
     * @param surveyResponsesId UUID
     */
    public ResponseSurveyResponses getDetail(UUID surveyResponsesId) {
        var surveyResponses = getSurveyResponses(surveyResponsesId);
        String originalRole = getUserOriginalRole(surveyResponses.getUser());
        return surveyResponsesMapper.toResponse(surveyResponses, originalRole);
    }

    public void submitFormSurvey(UUID surveyId, String surveyData, MultipartFile file) {
        User user = getCurrentUser();
        String pdfUrl = fileService.uploadFile(file, SURVEY_FORM_RESOURCE_KEY);
        Survey survey = surveyRepository
            .findById(surveyId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Survey")));
        SurveyResponses surveyResponse = new SurveyResponses();
        surveyResponse.setSurveyData(surveyData);
        surveyResponse.setPdfUrl(pdfUrl);
        surveyResponse.setUser(user);
        surveyResponse.setSubmissionDate(Instant.now());
        surveyResponse.setSurvey(survey);
        surveyResponsesRepository.save(surveyResponse);
    }

    private User getCurrentUser() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin).orElse(null);
    }
}
