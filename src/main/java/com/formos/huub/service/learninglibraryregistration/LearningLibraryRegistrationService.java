package com.formos.huub.service.learninglibraryregistration;

import com.formos.huub.config.ApplicationProperties;
import com.formos.huub.domain.constant.BusinessConstant;
import com.formos.huub.domain.constant.EmailTemplatePathsConstants;
import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.EntryTypeEnum;
import com.formos.huub.domain.enums.RegistrationStatusEnum;
import com.formos.huub.domain.request.Learninglibraryregistration.RequestReviewRegistration;
import com.formos.huub.domain.request.Learninglibraryregistration.RequestSearchLearningLibraryRegistration;
import com.formos.huub.domain.request.Learninglibraryregistration.RequestSearchLessonSurvey;
import com.formos.huub.domain.response.learninglibraryregistration.IResponseDetailLessonSurvey;
import com.formos.huub.domain.response.learninglibraryregistration.IResponseDetailRegistration;
import com.formos.huub.domain.response.learninglibraryregistration.ResponseDetailLessonSurvey;
import com.formos.huub.domain.response.learninglibraryregistration.ResponseDetailRegistration;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.constant.AppConstants;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.service.mail.IMailService;
import com.formos.huub.framework.service.storage.model.CloudProperties;
import com.formos.huub.helper.file.FileHelper;
import com.formos.huub.repository.BusinessOwnerRepository;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.mapper.learninglibraryregistration.LearningLibraryRegistrationMapper;
import com.formos.huub.repository.LearningLibraryRegistrationDetailRepository;
import com.formos.huub.repository.LearningLibraryRegistrationRepository;
import com.formos.huub.repository.LearningLibraryRepository;
import com.formos.huub.service.invite.InviteService;
import com.formos.huub.repository.UserAnswerFormRepository;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.formos.huub.domain.constant.BusinessConstant.NUMBER_0;
import static com.formos.huub.domain.constant.FormConstant.PORTAL_INTAKE_QUESTION_BUSINESS;
import static com.formos.huub.framework.constant.AppConstants.*;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LearningLibraryRegistrationService extends BaseService {

    IMailService mailService;

    ApplicationProperties applicationProperties;

    LearningLibraryRegistrationRepository learningLibraryRegistrationRepository;

    LearningLibraryRepository learningLibraryRepository;

    BusinessOwnerRepository businessOwnerRepository;

    InviteService inviteService;

    LearningLibraryRegistrationMapper learningLibraryRegistrationMapper;

    UserAnswerFormRepository userAnswerFormRepository;

    LearningLibraryRegistrationDetailRepository learningLibraryRegistrationDetailRepository;

    FileHelper fileHelper;

    private static final String COURSE_NAME = "courseName";
    private static final String BUSINESS_OWNER_NAME = "businessOwnerName";
    private static final String PORTAL_HOST_NAME_PATTERN = "%s %s"; // {firstName}{lastname}
    private static final String PORTAL_HOST_NAME = "portalHostName";
    private static final String REGISTRATION_ID = "registrationId";
    private static final String LEARNING_LIBRARY_ID = "learningLibraryId";
    private static final String LIVE_CHAT = "liveChat";

    /**
     * Search Registration By TermAndCondition
     *
     * @param request RequestSearchLearningLibraryRegistration
     * @return Map<String, Object>
     */
    public Map<String, Object> searchRegistrationByTermAndCondition(RequestSearchLearningLibraryRegistration request) {
        if (Objects.isNull(request.getPortalId())){
            request.setPortalId(PortalContextHolder.getPortalId());
        }
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "llr.registration_date,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        HashMap<String, String> sortMap = new HashMap<>();
        sortMap.put("businessOwnerName", "u.normalized_full_name");
        sortMap.put("courseName", "ll.name");
        sortMap.put("submissionDate", "llr.registration_date");
        sortMap.put("status", "llr.registration_status");
        sortMap.put("portalName", "p.platform_name");
        sortMap.put(BusinessConstant.TIMEZONE_KEY, request.getTimezone());
        request.setSearchConditions(ObjectUtils.convertSortParams(request.getSearchConditions(), sortMap));

        return PageUtils.toPage(learningLibraryRegistrationRepository.searchRegistrationByTermAndCondition(request, request.getPortalId(), pageable));
    }

    /**
     * Search Lesson Survey By TermAndCondition
     *
     * @param request RequestSearchLessonSurvey
     * @return Map<String, Object>
     */
    public Map<String, Object> searchLessonSurveyByTermAndCondition(RequestSearchLessonSurvey request) {
        if (Objects.isNull(request.getPortalId())){
            request.setPortalId(PortalContextHolder.getPortalId());
        }
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "llrd.submission_date,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        HashMap<String, String> sortMap = new HashMap<>();
        sortMap.put("businessOwnerName", "u.normalized_full_name");
        sortMap.put("courseName", "ll.name");
        sortMap.put("lessonName", "lll.title");
        sortMap.put("submissionDate", "llrd.submission_date");
        sortMap.put("portalName", "p.platform_name");
        sortMap.put(BusinessConstant.TIMEZONE_KEY, request.getTimezone());
        request.setSearchConditions(ObjectUtils.convertSortParams(request.getSearchConditions(), sortMap));

        return PageUtils.toPage(learningLibraryRegistrationRepository.searchLessonSurveyByTermAndCondition(request, request.getPortalId(), pageable));
    }

    /**
     * Get Detail Registration
     *
     * @param id UUID
     * @return ResponseDetailRegistration
     */
    public ResponseDetailRegistration getDetailRegistration(UUID id) {
        LearningLibraryRegistration registration = getRegistration(id);
        LearningLibrary learningLibrary = registration.getLearningLibrary();
        IResponseDetailRegistration detailRegistration = learningLibraryRegistrationRepository.getRegistrationById(id);
        ResponseDetailRegistration response = learningLibraryRegistrationMapper.interfaceToResponseDetailRegistration(detailRegistration);
        String businessName = getBusinessName(registration.getUser().getId());
        response.setBusinessName(businessName);

        if (Objects.isNull(learningLibrary.getEnrollmentDeadline())) {
            response.setEnrollmentDeadline(getEnrollmentDeadline(learningLibrary));
        }

        return response;
    }

    /**
     * Get Enrollment Deadline
     *
     * @param learningLibrary LearningLibrary
     * @return Instant
     */
    private Instant getEnrollmentDeadline(LearningLibrary learningLibrary) {
        Instant publishedDate = learningLibrary.getPublishedDate();
        Integer expiresInDays = learningLibrary.getExpiresIn();
        if (Objects.nonNull(publishedDate) && Objects.nonNull(expiresInDays) && expiresInDays > NUMBER_0) {
            return publishedDate.plus(expiresInDays, ChronoUnit.DAYS);
        }
        return null;
    }

    /**
     * Get Detail LessonSurvey
     *
     * @param id UUID
     * @return ResponseDetailLessonSurvey
     */
    public ResponseDetailLessonSurvey getDetailLessonSurvey(UUID id) {
        LearningLibraryRegistrationDetail registrationDetail = getRegistrationDetail(id);
        UUID userId = registrationDetail.getLearningLibraryRegistration().getUser().getId();
        IResponseDetailLessonSurvey detailLessonSurvey = learningLibraryRegistrationRepository.getLessonSurveyById(id);
        ResponseDetailLessonSurvey response = learningLibraryRegistrationMapper.interfaceToResponseDetailLessonSurvey(detailLessonSurvey);

        JsonElement contents = detailLessonSurvey.getContents();
        String surveyJson = null;
        if (Objects.nonNull(contents) && contents.isJsonObject()) {
            JsonObject jsonObject = contents.getAsJsonObject();
            if (jsonObject.has("surveyContent")) {
                surveyJson = jsonObject.get("surveyContent").getAsString();
            }
        }
        response.setSurveyJson(surveyJson);

        String businessName = getBusinessName(userId);
        response.setBusinessName(businessName);

        return response;
    }

    /**
     * Get Business Name
     *
     * @param userId UUID
     * @return String
     */
    public String getBusinessName(UUID userId) {
        Optional<UserAnswerForm> userAnswerFormOpt = userAnswerFormRepository.findByQuestionCodeAndEntryIdAndEntryType(
            PORTAL_INTAKE_QUESTION_BUSINESS,
            userId,
            EntryTypeEnum.USER
        );
        return userAnswerFormOpt
            .map(UserAnswerForm::getAdditionalAnswer)
            .orElse(null);
    }

    /**
     * Get Registration
     *
     * @param id UUID
     * @return LearningLibraryRegistration
     */
    private LearningLibraryRegistration getRegistration(UUID id) {
        return learningLibraryRegistrationRepository.findById(id)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Registration")));
    }

    /**
     * Get Registration Detail
     *
     * @param id UUID
     * @return LearningLibraryRegistrationDetail
     */
    private LearningLibraryRegistrationDetail getRegistrationDetail(UUID id) {
        return learningLibraryRegistrationDetailRepository.findById(id)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Lesson Survey")));
    }

    /**
     * Review Course Registration
     *
     * @param request RequestReviewRegistration
     */
    public void reviewCourseRegistration(RequestReviewRegistration request) {
        UUID registrationId = UUID.fromString(request.getRegistrationId());
        RegistrationStatusEnum status = request.getRegistrationStatus();
        LearningLibraryRegistration registration = getLearningLibraryRegistration(registrationId);
        LearningLibrary learningLibrary = registration.getLearningLibrary();

        User user = registration.getUser();
        BusinessOwner businessOwner = businessOwnerRepository.findByUserEmailIgnoreCase(user.getEmail())
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Business Owner")));
        Portal portal = businessOwner.getPortal();

        if (RegistrationStatusEnum.APPROVED.equals(status)) {
            registration.setRegistrationStatus(RegistrationStatusEnum.APPROVED);

            sendCourseRegistrationApprovalEmail(user, learningLibrary, portal);
        } else if (RegistrationStatusEnum.DENIED.equals(status)) {
            registration.setRegistrationStatus(RegistrationStatusEnum.DENIED);

            int currentRegistered = Objects.nonNull(learningLibrary.getNumberOfRegistered()) ? learningLibrary.getNumberOfRegistered() : 0;
            learningLibrary.setNumberOfRegistered(currentRegistered - 1);
            learningLibraryRepository.save(learningLibrary);

            sendCourseRegistrationDeniedEmail(user, learningLibrary, portal);
        }
        registration.setDecisionDate(Instant.now());
        learningLibraryRegistrationRepository.save(registration);
    }

    /**
     * Get LearningLibrary Registration
     *
     * @param registrationId UUID
     * @return LearningLibraryRegistration
     */
    private LearningLibraryRegistration getLearningLibraryRegistration(UUID registrationId) {
        return learningLibraryRegistrationRepository
            .findById(registrationId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Learning Library Registration")));
    }

    /**
     * Send Course Registration Email
     *
     * @param portalHosts     List<PortalHost>
     * @param user            User
     * @param learningLibrary LearningLibrary
     * @param registrationId  UUID
     */
    public void sendCourseRegistrationEmail(List<PortalHost> portalHosts, User user, LearningLibrary learningLibrary, UUID registrationId) {
        String templatePath = EmailTemplatePathsConstants.COURSE_REGISTRATION;
        String title = MessageHelper.getMessage("email.course.registration.title", List.of());

        portalHosts.forEach(portalHost -> {
            String clientAppUrl = inviteService.buildPortalUrl(portalHost.getPortal());
            HashMap<String, Object> mapContents = new HashMap<>();
            mapContents.put(BASE_URL, clientAppUrl);
            mapContents.put(COURSE_NAME, learningLibrary.getName());
            mapContents.put(BUSINESS_OWNER_NAME, user.getNormalizedFullName());
            mapContents.put(REGISTRATION_ID, registrationId);
            mapContents.put(LOGO_IMAGE, fileHelper.primaryPortalLogo(portalHost.getPortal().getPrimaryLogo()));
            mapContents.put(PORTAL_HOST_NAME, String.format(PORTAL_HOST_NAME_PATTERN, portalHost.getFirstName(), portalHost.getLastName()));

            mailService.sendEmailFromTemplate(portalHost.getEmail(), AppConstants.DEFAULT_LANGUAGE, mapContents, templatePath, title);
        });
    }

    /**
     * Send Course Registration Approval Email
     *
     * @param user            User
     * @param learningLibrary LearningLibrary
     */
    public void sendCourseRegistrationApprovalEmail(User user, LearningLibrary learningLibrary, Portal portal) {
        String clientAppUrl = inviteService.buildPortalUrl(portal);
        String templatePath = EmailTemplatePathsConstants.COURSE_REGISTRATION_APPROVAL;
        String courseName = learningLibrary.getName();
        String learningLibraryId = learningLibrary.getId().toString();
        String title = MessageHelper.getMessage("email.course.registration.approval.title", List.of(courseName));
        String titleReplace = String.join("", "[", courseName, "]");
        title = title.replace(titleReplace, courseName);

        HashMap<String, Object> mapContents = new HashMap<>();
        mapContents.put(BASE_URL, clientAppUrl);
        mapContents.put(COURSE_NAME, courseName);
        mapContents.put(BUSINESS_OWNER_NAME, user.getNormalizedFullName());
        mapContents.put(LEARNING_LIBRARY_ID, learningLibraryId);
        mapContents.put(LOGO_IMAGE, fileHelper.primaryPortalLogo(portal.getPrimaryLogo()));
        mailService.sendEmailFromTemplate(user.getEmail(), AppConstants.DEFAULT_LANGUAGE, mapContents, templatePath, title);
    }

    /**
     * Send Course Registration Denied Email
     *
     * @param user            User
     * @param learningLibrary LearningLibrary
     */
    public void sendCourseRegistrationDeniedEmail(User user, LearningLibrary learningLibrary, Portal portal) {
        String clientAppUrl = inviteService.buildPortalUrl(portal);
        String liveChat = applicationProperties.getCustomerCare().getJoinHuubLiveChat();
        String templatePath = EmailTemplatePathsConstants.COURSE_REGISTRATION_DENIED;
        String courseName = learningLibrary.getName();
        String title = MessageHelper.getMessage("email.course.registration.denied.title", List.of());

        HashMap<String, Object> mapContents = new HashMap<>();
        mapContents.put(BASE_URL, clientAppUrl);
        mapContents.put(LIVE_CHAT, liveChat);
        mapContents.put(COURSE_NAME, courseName);
        mapContents.put(BUSINESS_OWNER_NAME, user.getNormalizedFullName());
        mapContents.put(LOGO_IMAGE, fileHelper.primaryPortalLogo(portal.getPrimaryLogo()));
        mailService.sendEmailFromTemplate(user.getEmail(), AppConstants.DEFAULT_LANGUAGE, mapContents, templatePath, title);
    }

}
