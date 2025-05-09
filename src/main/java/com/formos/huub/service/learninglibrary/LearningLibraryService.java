package com.formos.huub.service.learninglibrary;

import com.formos.huub.domain.constant.ActiveCampaignConstant;
import com.formos.huub.domain.constant.BusinessConstant;
import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.*;
import com.formos.huub.domain.request.learninglibrary.*;
import com.formos.huub.domain.response.learninglibrary.*;
import com.formos.huub.domain.response.portals.ResponseNewlyFeature;
import com.formos.huub.framework.constant.AppConstants;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.enums.DateTimeFormat;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.*;
import com.formos.huub.mapper.learninglibrary.LearningLibraryLessonMapper;
import com.formos.huub.mapper.learninglibrary.LearningLibraryMapper;
import com.formos.huub.mapper.learninglibrary.LearningLibrarySectionMapper;
import com.formos.huub.mapper.learninglibrary.LearningLibraryStepMapper;
import com.formos.huub.mapper.tag.TagMapper;
import com.formos.huub.mapper.userlearninglibrary.UserLearningLibraryMapper;
import com.formos.huub.repository.*;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.activecampaign.ActiveCampaignStrategy;
import com.formos.huub.service.common.JsonElementService;
import com.formos.huub.service.file.FileService;
import com.formos.huub.service.learninglibraryregistration.LearningLibraryRegistrationService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.formos.huub.domain.constant.BusinessConstant.NUMBER_0;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LearningLibraryService {

    LearningLibraryRepository learningLibraryRepository;

    LearningLibraryMapper learningLibraryMapper;

    TagMapper tagMapper;

    TagRepository tagRepository;

    CategoryRepository categoryRepository;

    PortalRepository portalRepository;

    SpeakerRepository speakerRepository;

    LearningLibraryStepMapper learningLibraryStepMapper;

    LearningLibraryStepRepository learningLibraryStepRepository;

    LearningLibraryLessonMapper learningLibraryLessonMapper;

    LearningLibraryLessonRepository learningLibraryLessonRepository;

    LearningLibrarySectionRepository learningLibrarySectionRepository;

    LearningLibrarySectionMapper learningLibrarySectionMapper;

    JsonElementService jsonElementService;

    FileService fileService;

    TermsAndConditionsRepository termsAndConditionsRepository;

    UserRepository userRepository;

    TermsAcceptanceRepository termsAcceptanceRepository;

    UserLearningLibraryRepository userLearningLibraryRepository;

    UserLearningLibraryMapper userLearningLibraryMapper;

    LearningLibraryRegistrationRepository learningLibraryRegistrationRepository;

    LearningLibraryRegistrationService learningLibraryRegistrationService;

    PortalHostRepository portalHostRepository;

    LearningLibraryRegistrationDetailRepository learningLibraryRegistrationDetailRepository;

    ActiveCampaignStrategy activeCampaignStrategy;

    private static final String LEARNING_LIBRARY_RESOURCE_KEY = "learningLibrary/resource/";

    private static final Integer MAX_SECTION_FILES = 4;

    private static final String SURVEY_FORM_RESOURCE_KEY = "surveyForm";

    /**
     * search Learning Libraries
     *
     * @param request RequestSearchLearningLibrary
     * @return Map<String, Object>
     */
    public Map<String, Object> searchLearningLibraries(RequestSearchLearningLibrary request) {
        var portalContext = PortalContextHolder.getContext();
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "createdDate,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        var data = learningLibraryRepository.searchByTermAndCondition(request, portalContext.getPortalId(), pageable);
        return PageUtils.toPage(data);
    }

    /**
     * Search Learning Libraries card view
     *
     * @param request RequestSearchLearningLibrary
     * @return Map<String, Object>
     */
    public Map<String, Object> searchLearningLibrariesCardView(RequestSearchLearningLibrary request) {
        var portalContext = PortalContextHolder.getContext();
        var user = getCurrentUser();
        var userId = getCurrentUser() != null ? user.getId() : null;
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "createdDate,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        var data = learningLibraryRepository
            .searchByTermAndConditionCardView(request, userId, portalContext.getPortalId(), pageable);
        return PageUtils.toPage(data);
    }

    /**
     * Bookmark Learning Library
     *
     * @param request RequestBookmarkLearningLibrary
     * @return ResponseBookmarkLearningLibrary
     */
    public ResponseBookmarkLearningLibrary bookmarkLearningLibrary(RequestBookmarkLearningLibrary request) {
        UUID learningLibraryId = request.getLearningLibraryId();
        User user = getCurrentUser();

        UserLearningLibrary userLearningLibrary = getUserLearningLibrary(user.getId(), learningLibraryId)
            .map(ull -> {
                ull.setIsBookmark(request.getIsBookmark());
                return ull;
            })
            .orElseGet(() -> {
                LearningLibrary learningLibrary = getLearningLibrary(learningLibraryId);
                return userLearningLibraryMapper.toEntityBookmark(user, learningLibrary, request.getIsBookmark());
            });

        userLearningLibraryRepository.save(userLearningLibrary);
        return userLearningLibraryMapper.toResponseBookmark(userLearningLibrary, learningLibraryId);
    }

    /**
     * Get User Learning Library
     *
     * @param userId            UUID
     * @param learningLibraryId UUID
     * @return Optional<UserLearningLibrary>
     */
    public Optional<UserLearningLibrary> getUserLearningLibrary(UUID userId, UUID learningLibraryId) {
        return userLearningLibraryRepository.findById_User_IdAndId_LearningLibrary_Id(userId, learningLibraryId);
    }

    /**
     * get Detail Learning library by ID
     *
     * @param learningLibraryId UUID
     * @return ResponseLearningLibraryAbout
     */
    public ResponseLearningLibraryDetail getDetail(UUID learningLibraryId) {
        var learningLibrary = getLearningLibrary(learningLibraryId);
        var about = learningLibraryMapper.toResponse(learningLibrary);
        var registration = learningLibraryMapper.toResponseRegistration(learningLibrary);
        var stepsAndLessons = learningLibraryMapper.toResponseSteps(learningLibrary.getLearningLibrarySteps());
        var speakers = learningLibrary.getSpeakers().stream().map(Speaker::getId).toList();
        return ResponseLearningLibraryDetail.builder()
            .id(learningLibraryId)
            .about(about)
            .registration(registration)
            .speakers(speakers)
            .learningLibrarySteps(stepsAndLessons)
            .build();
    }

    public ResponseOverviewLearningLibrary getOverview(UUID learningLibraryId) {
        var learningLibrary = getLearningLibrary(learningLibraryId);
        User user = getCurrentUser();
        RegistrationStatusEnum registrationStatus = getRegistrationStatusOfLearningLibrary(user, learningLibrary);

        var response = learningLibraryMapper.toResponseOverview(learningLibrary);

        List<UUID> lessonIds = extractLessonIds(learningLibrary);
        Map<UUID, LearningLibraryRegistrationDetail> registrationDetailMap = fetchRegistrationDetails(user.getId(), lessonIds);
        List<ResponseLearningLibraryStep> stepsAndLessons = learningLibrary.getLearningLibrarySteps().stream()
            .map(step -> mapStepWithLessons(step, registrationDetailMap))
            .toList();

        response.setLearningLibrarySteps(stepsAndLessons);
        response.setLastActivityDate(getLastActivityLearningLibrary(user, learningLibrary));
        response.setNumLessons(getNumLessonOfCourse(response.getLearningLibrarySteps()));
        response.setNumDownloads(getNumDownloadsOfCourse(learningLibraryId));
        response.setRegistrationStatus(registrationStatus);

        if (Objects.isNull(learningLibrary.getEnrollmentDeadline())) {
            response.setEnrollmentDeadline(getEnrollmentDeadline(learningLibrary));
        }

        return response;
    }

    public ResponseRecommendCourse getRecommendCourse(UUID portalId) {
        Pageable topOne = PageRequest.of(0, 1);
        var latestLearningLibraries = learningLibraryRepository.findLatestByPortal(portalId, topOne);

        if (latestLearningLibraries.isEmpty()) {
            return null;
        }
        var latest = latestLearningLibraries.get(NUMBER_0);
        return toResponseBaseCourse(latest);
    }

    public List<ResponseNewlyFeature> getNewlyCourse(UUID portalId, Integer size) {
        Pageable topOne = PageRequest.of(0, size);
        return learningLibraryRepository.findLatestByPortal(portalId, topOne).stream().map(this::toResponseNewlyCourse).toList();
    }

    private ResponseNewlyFeature toResponseNewlyCourse(LearningLibrary learningLibrary) {
        var responseBuilder = ResponseNewlyFeature.builder()
            .id(learningLibrary.getId())
            .title(learningLibrary.getName())
            .description(learningLibrary.getDescription())
            .imageUrl(learningLibrary.getHeroImage());
        var category = learningLibrary.getCategory();
        if (Objects.nonNull(learningLibrary.getCategory())) {
            responseBuilder.categoryIcon(category.getIconUrl());
        }
        return responseBuilder.build();
    }

    private ResponseRecommendCourse toResponseBaseCourse(LearningLibrary learningLibrary) {
        return ResponseRecommendCourse.builder()
            .id(learningLibrary.getId())
            .name(learningLibrary.getName())
            .startDate(learningLibrary.getStartDate())
            .categoryName(
                Optional.ofNullable(learningLibrary.getCategory())
                    .map(Category::getName)
                    .orElse(StringUtils.EMPTY)
            )
            .imageUrl(learningLibrary.getHeroImage())
            .build();
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
     * Get RegistrationStatus Of LearningLibrary
     *
     * @param user            User
     * @param learningLibrary LearningLibrary
     * @return RegistrationStatusEnum
     */
    private RegistrationStatusEnum getRegistrationStatusOfLearningLibrary(User user, LearningLibrary learningLibrary) {
        return learningLibraryRegistrationRepository
            .findByUserIdAndLearningLibraryId(user.getId(), learningLibrary.getId())
            .map(LearningLibraryRegistration::getRegistrationStatus)
            .orElse(null);
    }

    /**
     * Register Learning Library
     *
     * @param learningLibraryId UUID
     * @param isReviewRequired  UUID
     * @param surveyData        String
     * @param file              MultipartFile
     */
    public void registerLearningLibrary(UUID learningLibraryId, String isReviewRequired, String surveyData, MultipartFile file, String timezone) {
        User user = getCurrentUser();
        LearningLibrary learningLibrary = getLearningLibrary(learningLibraryId);
        validateLearningLibraryRegistration(learningLibrary, timezone);
        String pdfUrl = Objects.nonNull(file) ? fileService.uploadFile(file, SURVEY_FORM_RESOURCE_KEY) : null;
        boolean isNeedReview = Boolean.parseBoolean(isReviewRequired);

        int currentRegistered = Objects.nonNull(learningLibrary.getNumberOfRegistered()) ? learningLibrary.getNumberOfRegistered() : 0;
        learningLibrary.setNumberOfRegistered(currentRegistered + 1);
        learningLibraryRepository.save(learningLibrary);

        LearningLibraryRegistration learningLibraryRegistration =
            learningLibraryRegistrationRepository.findByUserIdAndLearningLibraryId(user.getId(), learningLibraryId)
                .orElseGet(LearningLibraryRegistration::new);
        learningLibraryRegistration.setRegistrationStatus(isNeedReview ? RegistrationStatusEnum.SUBMITTED : RegistrationStatusEnum.APPROVED);
        learningLibraryRegistration.setRegistrationDate(Instant.now());
        learningLibraryRegistration.setSurveyData(surveyData);
        learningLibraryRegistration.setPdfUrl(pdfUrl);
        learningLibraryRegistration.setUser(user);
        learningLibraryRegistration.setLearningLibrary(learningLibrary);
        learningLibraryRegistration.setCourseType(learningLibrary.getAccessType());
        learningLibraryRegistrationRepository.save(learningLibraryRegistration);

        // Handle send course registration email
        if (isNeedReview) {
            List<UUID> portalIds = learningLibrary.getPortals().stream().map(Portal::getId).toList();
            List<PortalHost> portalHosts = portalHostRepository.getAllByPortalIdInAndStatus(portalIds, PortalHostStatusEnum.ACTIVE);
            learningLibraryRegistrationService.sendCourseRegistrationEmail(portalHosts, user, learningLibrary, learningLibraryRegistration.getId());

            activeCampaignStrategy.handleIncrementContactField(user, ActiveCampaignConstant.FIELD_COURSE_COUNT_V2);
        }
    }

    /**
     * Validate Learning Library Registration
     *
     * @param learningLibrary LearningLibrary
     */
    private void validateLearningLibraryRegistration(LearningLibrary learningLibrary, String timezone) {
        validateEnrollLimit(learningLibrary);

        if (AccessTypeEnum.FREE.equals(learningLibrary.getAccessType())) {
            return;
        }

        validateEnrollmentDeadline(learningLibrary, timezone);
    }

    /**
     * Validate EnrollLimit
     *
     * @param learningLibrary LearningLibrary
     */
    private void validateEnrollLimit(LearningLibrary learningLibrary) {
        int enrolledLimit = Optional.ofNullable(learningLibrary.getEnrolleeLimit()).orElse(0);
        int numberOfRegistered = Optional.ofNullable(learningLibrary.getNumberOfRegistered()).orElse(0);

        if (enrolledLimit > 0 && numberOfRegistered > enrolledLimit) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0052));
        }
    }

    /**
     * Validate EnrollmentDeadline
     *
     * @param learningLibrary LearningLibrary
     * @param timezone        String
     */
    private void validateEnrollmentDeadline(LearningLibrary learningLibrary, String timezone) {
        LocalDate today = LocalDate.now(ZoneId.of(timezone));

        Instant enrollmentDeadline = Optional.ofNullable(learningLibrary.getEnrollmentDeadline())
            .orElseGet(() -> getEnrollmentDeadline(learningLibrary));

        LocalDate deadlineDate = enrollmentDeadline.atZone(ZoneOffset.UTC).toLocalDate();

        if (today.isAfter(deadlineDate)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0053));
        }
    }

    /**
     * Submit lesson form survey
     *
     * @param learningLibraryId UUID
     * @param lessonId          UUID
     * @param surveyData        String
     * @param file              MultipartFile
     */
    public void submitLessonFormSurvey(UUID learningLibraryId, UUID lessonId, String surveyData, MultipartFile file) {
        User user = getCurrentUser();
        String pdfUrl = fileService.uploadFile(file, SURVEY_FORM_RESOURCE_KEY);
        LearningLibraryRegistration learningLibraryRegistration =
            learningLibraryRegistrationRepository.findByUserIdAndLearningLibraryId(user.getId(), learningLibraryId)
                .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Registration")));
        LearningLibraryRegistrationDetail registrationDetail = learningLibraryRegistrationDetailRepository
            .findByUserAndLessonId(user.getId(), lessonId)
            .orElseGet(LearningLibraryRegistrationDetail::new);
        registrationDetail.setSurveyData(surveyData);
        registrationDetail.setPdfUrl(pdfUrl);
        registrationDetail.setLearningStatus(LearningStatusEnum.STARTED);
        registrationDetail.setLessonId(lessonId);
        registrationDetail.setLearningLibraryRegistration(learningLibraryRegistration);
        registrationDetail.setSubmissionDate(Instant.now());
        learningLibraryRegistrationDetailRepository.save(registrationDetail);
    }

    /**
     * Update Lesson Status
     *
     * @param request RequestUpdateLessonStatus
     */
    public void updateLessonStatus(RequestUpdateLessonStatus request) {
        UUID learningLibraryId = request.getLearningLibraryId();
        UUID lessonId = request.getLessonId();
        LearningStatusEnum learningStatus = request.getLearningStatus();
        Instant completionDate = learningStatus.equals(LearningStatusEnum.COMPLETE) ? Instant.now() : null;
        User user = getCurrentUser();
        LearningLibraryRegistration learningLibraryRegistration =
            learningLibraryRegistrationRepository.findByUserIdAndLearningLibraryId(user.getId(), learningLibraryId)
                .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Registration")));
        learningLibraryRegistration.setLastActivityDate(Instant.now());
        learningLibraryRegistrationRepository.save(learningLibraryRegistration);

        LearningLibraryRegistrationDetail registrationDetail = learningLibraryRegistrationDetailRepository
            .findByUserAndLessonId(user.getId(), lessonId)
            .orElseGet(LearningLibraryRegistrationDetail::new);
        registrationDetail.setLearningStatus(learningStatus);
        registrationDetail.setLessonId(lessonId);
        registrationDetail.setLearningLibraryRegistration(learningLibraryRegistration);
        registrationDetail.setCompletionDate(completionDate);
        learningLibraryRegistrationDetailRepository.save(registrationDetail);

        // Handle course completion
        if (learningStatus.equals(LearningStatusEnum.COMPLETE)) {
            handleCourseCompletion(learningLibraryId);
        }
    }

    /**
     * Handle course completion
     *
     * @param learningLibraryId UUID
     */
    public void handleCourseCompletion(UUID learningLibraryId) {
        User user = getCurrentUser();
        int numberOfLessons = learningLibraryLessonRepository.countLessonInCourseById(learningLibraryId);
        int numberOfCompleteLessons = learningLibraryRegistrationDetailRepository.countCompleteLessonByLearningLibraryId(user.getId(), learningLibraryId);
        LearningStatusEnum learningStatus = numberOfCompleteLessons < numberOfLessons ? LearningStatusEnum.STARTED : LearningStatusEnum.COMPLETE;

        UserLearningLibrary userLearningLibrary = getUserLearningLibrary(user.getId(), learningLibraryId)
            .map(ull -> {
                ull.setCompletionStatus(learningStatus);
                return ull;
            })
            .orElseGet(() -> {
                LearningLibrary learningLibrary = getLearningLibrary(learningLibraryId);
                return userLearningLibraryMapper.toCompletionStatusEntity(user, learningLibrary, learningStatus);
            });

        userLearningLibraryRepository.save(userLearningLibrary);
    }

    /**
     * Get Detail Course
     *
     * @param learningLibraryId UUID
     * @return ResponseDetailLearningLibrary
     */
    public ResponseDetailLearningLibrary getDetailCourse(UUID learningLibraryId) {
        User user = getCurrentUser();
        var learningLibrary = getLearningLibrary(learningLibraryId);
        ResponseDetailLearningLibrary response = learningLibraryMapper.toResponseDetail(learningLibrary);

        List<UUID> lessonIds = extractLessonIds(learningLibrary);
        Map<UUID, LearningLibraryRegistrationDetail> registrationDetailMap = fetchRegistrationDetails(user.getId(), lessonIds);
        List<ResponseLearningLibraryStep> stepsAndLessons = learningLibrary.getLearningLibrarySteps().stream()
            .map(step -> mapStepWithLessons(step, registrationDetailMap))
            .toList();

        response.setLearningLibrarySteps(stepsAndLessons);
        response.setLastActivityDate(getLastActivityLearningLibrary(user, learningLibrary));
        return response;
    }

    /**
     * Get LastActivity LearningLibrary
     *
     * @param user            User
     * @param learningLibrary LearningLibrary
     * @return Instant
     */
    private Instant getLastActivityLearningLibrary(User user, LearningLibrary learningLibrary) {
        return learningLibraryRegistrationRepository
            .findByUserIdAndLearningLibraryId(user.getId(), learningLibrary.getId())
            .map(LearningLibraryRegistration::getLastActivityDate)
            .orElse(null);
    }

    /**
     * Extract LessonIds
     *
     * @param learningLibrary LearningLibrary
     * @return List<UUID>
     */
    private List<UUID> extractLessonIds(LearningLibrary learningLibrary) {
        return learningLibrary.getLearningLibrarySteps().stream()
            .flatMap(step -> step.getLearningLibraryLessons().stream())
            .map(LearningLibraryLesson::getId)
            .toList();
    }

    /**
     * Fetch RegistrationDetails
     *
     * @param userId    UUID
     * @param lessonIds List<UUID>
     * @return Map<UUID, LearningLibraryRegistrationDetail>
     */
    private Map<UUID, LearningLibraryRegistrationDetail> fetchRegistrationDetails(UUID userId, List<UUID> lessonIds) {
        return learningLibraryRegistrationDetailRepository.findByUserAndLessonIdIn(userId, lessonIds).stream()
            .collect(Collectors.toMap(LearningLibraryRegistrationDetail::getLessonId, detail -> detail));
    }

    /**
     * Map Step With Lessons
     *
     * @param step                  LearningLibraryStep
     * @param registrationDetailMap Map<UUID, LearningLibraryRegistrationDetail>
     * @return ResponseLearningLibraryStep
     */
    private ResponseLearningLibraryStep mapStepWithLessons(LearningLibraryStep step, Map<UUID, LearningLibraryRegistrationDetail> registrationDetailMap) {
        var stepResponse = learningLibraryMapper.toResponseStep(step);

        List<ResponseLearningLibraryLesson> updatedLessons = step.getLearningLibraryLessons().stream()
            .map(lesson -> mapLessonWithRegistration(lesson, registrationDetailMap))
            .toList();

        stepResponse.setLearningLibraryLessons(updatedLessons);

        stepResponse.setLearningStatus(determineStepStatus(updatedLessons));
        return stepResponse;
    }

    /**
     * Map Lesson With Registration
     *
     * @param lesson                LearningLibraryLesson
     * @param registrationDetailMap Map<UUID, LearningLibraryRegistrationDetail>
     * @return ResponseLearningLibraryLesson
     */
    private ResponseLearningLibraryLesson mapLessonWithRegistration(LearningLibraryLesson lesson, Map<UUID, LearningLibraryRegistrationDetail> registrationDetailMap) {
        var lessonResponse = learningLibraryLessonMapper.toResponse(lesson);
        var registrationDetail = Optional.ofNullable(registrationDetailMap.get(lesson.getId()));

        lessonResponse.setSurveyData(registrationDetail.map(LearningLibraryRegistrationDetail::getSurveyData).orElse(null));
        lessonResponse.setLearningStatus(registrationDetail.map(LearningLibraryRegistrationDetail::getLearningStatus).orElse(LearningStatusEnum.NOT_STARTED));
        return lessonResponse;
    }

    /**
     * Determine Step Status
     *
     * @param lessons List<ResponseLearningLibraryLesson>
     * @return LearningStatusEnum
     */
    private LearningStatusEnum determineStepStatus(List<ResponseLearningLibraryLesson> lessons) {
        List<LearningStatusEnum> statuses = lessons.stream()
            .map(ResponseLearningLibraryLesson::getLearningStatus)
            .toList();

        if (statuses.stream().allMatch(status -> status == LearningStatusEnum.COMPLETE)) {
            return LearningStatusEnum.COMPLETE;
        } else if (statuses.stream().anyMatch(status -> status == LearningStatusEnum.STARTED || status == LearningStatusEnum.COMPLETE)) {
            return LearningStatusEnum.STARTED;
        } else {
            return LearningStatusEnum.NOT_STARTED;
        }
    }


    private Integer getNumLessonOfCourse(List<ResponseLearningLibraryStep> learningLibrarySteps) {
        AtomicReference<Integer> count = new AtomicReference<>(0);
        learningLibrarySteps.forEach(ele -> {
            count.updateAndGet(v -> v + ele.getLearningLibraryLessons().size());
        });
        return count.get();
    }

    private Integer getNumDownloadsOfCourse(UUID learningLibraryId) {
        return learningLibrarySectionRepository.numDownloadsInLearningLibrary(learningLibraryId);
    }

    /**
     * delete Learning Library
     *
     * @param learningLibraryId UUID
     */
    public void deleteLearningLibrary(UUID learningLibraryId) {
        //TODO check learning library is use

        learningLibraryRepository.deleteById(learningLibraryId);
    }

    /**
     * create Learning Library
     *
     * @param request RequestCreateLearningLibrary
     */
    public UUID createLearningLibrary(RequestCreateLearningLibrary request) {
        boolean isDraft = Boolean.TRUE.equals(request.getIsDraft());
        if (!isDraft) {
            validateLearningLibraryContentType(request.getAbout(), request.getLearningLibrarySteps());
        }

        User user = getCurrentUser();
        RequestLearningLibraryRegistration registration = request.getRegistration();

        // handel learning library about
        var learningLibrary = createLearningLibraryAbout(request.getAbout(), user);

        // handle learning library registration
        learningLibrary.setIsRegistrationFormRequired(registration.getIsRegistrationFormRequired());
        learningLibrary.setSurveyJson(registration.getSurveyJson());

        // handle learning library speakers
        Set<Speaker> speakers = getSpeakers(request.getSpeakers());
        learningLibrary.setSpeakers(speakers);

        // handle learning library steps and lessons
        saveDataLearningLibraryStep(learningLibrary, request.getLearningLibrarySteps());

        // handle set course published date
        if (LearningLibraryStatusEnum.PUBLISHED.equals(request.getAbout().getStatus())) {
            learningLibrary.setPublishedDate(Instant.now());
        }

        // handle save draft course
        if (isDraft) {
            learningLibrary.setStatus(LearningLibraryStatusEnum.DRAFT);
        }

        learningLibraryRepository.save(learningLibrary);

        handleTermsAndConditions(learningLibrary, user);

        return learningLibrary.getId();
    }

    /**
     * Handle Terms And Conditions
     *
     * @param learningLibrary LearningLibrary
     * @param user            User
     */
    public void handleTermsAndConditions(LearningLibrary learningLibrary, User user) {
        if (!SecurityUtils.hasCurrentUserAnyOfAuthorities(
            AuthoritiesConstants.PORTAL_HOST,
            AuthoritiesConstants.TECHNICAL_ADVISOR,
            AuthoritiesConstants.COMMUNITY_PARTNER)) {
            return;
        }

        TermsAndConditions termsAndConditions = termsAndConditionsRepository.getTermsAndConditionsByType(TermsAndConditionsTypeEnum.LEARNING_LIBRARY).orElse(null);
        if (Objects.isNull(termsAndConditions)) {
            return;
        }

        String markdownText = termsAndConditions.getText();
        Instant acceptanceDate = Instant.now();

        String additionalContent = buildAdditionalContent(user, learningLibrary, termsAndConditions, acceptanceDate);

        byte[] pdfContent = FileUtils.convertMarkdownToPdf(markdownText, additionalContent);
        String url = fileService.uploadPdfFile(pdfContent, "Terms", "terms_and_conditions.pdf");

        TermsAcceptance termsAcceptance = TermsAcceptance.builder()
            .acceptanceDate(acceptanceDate)
            .pdfUrl(url)
            .versionNumber(termsAndConditions.getVersionNumber())
            .user(user)
            .learningLibrary(learningLibrary)
            .termsAndConditions(termsAndConditions)
            .build();
        termsAcceptanceRepository.save(termsAcceptance);
    }

    /**
     * Build Additional Content
     *
     * @param user               User
     * @param learningLibrary    LearningLibrary
     * @param termsAndConditions TermsAndConditions
     * @param acceptanceDate     Instant
     * @return String
     */
    private String buildAdditionalContent(User user, LearningLibrary learningLibrary, TermsAndConditions termsAndConditions, Instant acceptanceDate) {
        StringBuilder additionalContent = new StringBuilder();
        additionalContent.append("Fullname: ");
        additionalContent.append(user.getNormalizedFullName());
        additionalContent.append("  \nCourse: ");
        additionalContent.append(learningLibrary.getName());
        additionalContent.append("  \nDate: ");
        additionalContent.append(formatDateTimeDefaultSystem(acceptanceDate));
        additionalContent.append("  \nT&C version: ");
        additionalContent.append(termsAndConditions.getVersionNumber());
        additionalContent.append("  \n");
        additionalContent.append("  \n");

        return additionalContent.toString();
    }

    /**
     * Format DateTime Default System
     *
     * @param instant Instant
     * @return String
     */
    private String formatDateTimeDefaultSystem(Instant instant) {
        return DateTimeFormatter.ofPattern(DateTimeFormat.MM_dd_yyyy_HH_mm_ss_a.getValue()).format(instant.atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    /**
     * upload Resource
     *
     * @param fileType      ResourceFileTypeEnum
     * @param multipartFile MultipartFile
     * @return String
     */
    public String uploadResource(ResourceFileTypeEnum fileType, MultipartFile multipartFile, boolean isPublic) {
        String fileKey = LEARNING_LIBRARY_RESOURCE_KEY.concat(fileType.getName());
        return fileService.uploadFile(multipartFile, fileKey, isPublic);
    }

    /**
     * create Learning Library About
     *
     * @param aboutRequest RequestLearningLibraryAbout
     * @return LearningLibrary
     */
    private LearningLibrary createLearningLibraryAbout(RequestLearningLibraryAbout aboutRequest, User user) {
        var learningLibrary = learningLibraryMapper.toEntity(aboutRequest);
        learningLibrary.setUserCreatedId(user.getId());

        var category = getCategory(aboutRequest.getCategoryId());
        learningLibrary.setCategory(category);

        //handel portal
        var portals = getListPortals(aboutRequest.getPortals());
        learningLibrary.setPortals(portals);

        //handel tags
        var tags = handleTags(aboutRequest.getTags());
        learningLibrary.setTags(tags);
        return learningLibrary;
    }

    /**
     * update Learning Library
     *
     * @param learningLibraryId UUID
     * @param request           RequestUpdateLearningLibrary
     */
    public void updateLearningLibrary(UUID learningLibraryId, RequestUpdateLearningLibrary request) {
        boolean isDraft = Boolean.TRUE.equals(request.getIsDraft());
        if (!isDraft) {
            validateLearningLibraryContentType(request.getAbout(), request.getLearningLibrarySteps());
        }

        User user = getCurrentUser();
        RequestLearningLibraryRegistration registration = request.getRegistration();

        // handel partial object learning library about
        var learningLibrary = updateLearningLibraryAbout(learningLibraryId, request.getAbout());

        // handle learning library registration
        learningLibrary.setIsRegistrationFormRequired(registration.getIsRegistrationFormRequired());
        learningLibrary.setSurveyJson(registration.getSurveyJson());

        // handle learning library speakers
        Set<Speaker> speakers = getSpeakers(request.getSpeakers());
        learningLibrary.setSpeakers(speakers);

        // handle learning library steps and lessons
        saveDataLearningLibraryStep(learningLibrary, request.getLearningLibrarySteps());

        // handle save draft course
        if (isDraft) {
            learningLibrary.setStatus(LearningLibraryStatusEnum.DRAFT);
        }

        learningLibraryRepository.save(learningLibrary);

        handleTermsAndConditions(learningLibrary, user);
    }

    public LearningLibrary updateLearningLibraryAbout(UUID learningLibraryId, RequestLearningLibraryAbout aboutRequest) {
        var learningLibrary = getLearningLibrary(learningLibraryId);
        LearningLibraryStatusEnum originalStatus = learningLibrary.getStatus();
        learningLibraryMapper.partialEntity(learningLibrary, aboutRequest);

        // handle category
        Category category = getCategory(aboutRequest.getCategoryId());
        learningLibrary.setCategory(category);

        // handle portal
        var portals = getListPortals(aboutRequest.getPortals());
        learningLibrary.setPortals(portals);

        // handle tags
        var tags = handleTags(aboutRequest.getTags());
        learningLibrary.setTags(tags);

        // handle set course published date
        LearningLibraryStatusEnum newStatus = LearningLibraryStatusEnum.valueOf(aboutRequest.getStatus());
        if (!LearningLibraryStatusEnum.PUBLISHED.equals(originalStatus) &&
            LearningLibraryStatusEnum.PUBLISHED.equals(newStatus)) {
            learningLibrary.setPublishedDate(Instant.now());
        }

        return learningLibrary;
    }

    /**
     * save Tags for learning library
     *
     * @param tagRequests List<RequestLearningLibraryTag>
     */
    private Set<Tag> handleTags(List<String> tagRequests) {
        if (ObjectUtils.isEmpty(tagRequests)) {
            return null;
        }
        // Step 1: Query existing tags from the database
        var existingTags = tagRepository.findByNameIn(tagRequests);
        var tagMap = existingTags.stream()
            .collect(Collectors.toMap(Tag::getName, Function.identity(), (existing, replacement) -> existing));

        // Step 2: Determine new tags to create
        var newTags = tagRequests.stream()
            .filter(ele -> Objects.isNull(tagMap.get(ele)))
            .distinct()
            .map(tagMapper::toEntity)
            .toList();
        existingTags.addAll(tagRepository.saveAll(newTags));

        // Step 3: Return the list of tags as a Set (both existing and new)
        return new HashSet<>(existingTags);

    }

    /**
     * get List Portals
     *
     * @param portalIds List<UUID>
     * @return Set<Portal>
     */
    private Set<Portal> getListPortals(List<UUID> portalIds) {
        if (ObjectUtils.isEmpty(portalIds)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0016, "Portals"));
        }
        var portals = portalRepository.findAllById(portalIds);
        if (portals.size() != portalIds.size()) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Portals"));
        }
        return new HashSet<>(portals);
    }

    /**
     * @param about                RequestLearningLibraryAbout
     * @param learningLibrarySteps List<RequestLearningLibraryStep>
     */
    private void validateLearningLibraryContentType(RequestLearningLibraryAbout about, List<RequestLearningLibraryStep> learningLibrarySteps) {

        if (ObjectUtils.isEmpty(learningLibrarySteps)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0015, "Steps"));
        }

        if (ContentTypeEnum.SINGLE.equals(about.getContentType()) && learningLibrarySteps.size() > BusinessConstant.NUMBER_1) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0042));
        }
    }

    /**
     * get Category by ID
     *
     * @param categoryId UUID
     * @return Category
     */
    private Category getCategory(UUID categoryId) {
        if (Objects.isNull(categoryId)) {
            return null;
        }
        return categoryRepository
            .findById(categoryId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Category")));
    }

    /**
     * get Learning Library
     *
     * @param learningLibraryId UUID
     * @return LearningLibrary
     */
    private LearningLibrary getLearningLibrary(UUID learningLibraryId) {
        return learningLibraryRepository
            .findById(learningLibraryId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Learning Library")));
    }

    /**
     * Get speakers
     *
     * @param speakerIds List<UUID>
     * @return Set<Speaker>
     */
    private Set<Speaker> getSpeakers(List<UUID> speakerIds) {
        if (ObjectUtils.isEmpty(speakerIds)) {
            return null;
        }
        return new HashSet<>(speakerRepository.findAllById(speakerIds));
    }

    /**
     * Save data learning library step
     *
     * @param learningLibrary      LearningLibrary
     * @param learningLibrarySteps List<RequestLearningLibraryStep>
     */
    private void saveDataLearningLibraryStep(LearningLibrary learningLibrary, List<RequestLearningLibraryStep> learningLibrarySteps) {
        Set<LearningLibraryStep> currentSteps = learningLibrary.getLearningLibrarySteps();

        if (ObjectUtils.isEmpty(learningLibrarySteps)) {
            learningLibraryStepRepository.deleteAll(currentSteps);
            return;
        }

        Map<UUID, LearningLibraryStep> currentStepMap = createCurrentStepMap(currentSteps);
        List<LearningLibraryStep> stepsToDelete = new ArrayList<>(currentSteps);

        for (RequestLearningLibraryStep request : learningLibrarySteps) {
            LearningLibraryStep learningLibraryStep = updateOrCreateStep(request, currentStepMap);
            if (Objects.nonNull(learningLibraryStep.getId())) {
                stepsToDelete.remove(learningLibraryStep);
            }
            learningLibraryStep.setLearningLibrary(learningLibrary);
            learningLibraryStep = learningLibraryStepRepository.save(learningLibraryStep);
            saveDataLearningLibraryLesson(learningLibraryStep, request.getLearningLibraryLessons());
        }

        learningLibraryStepRepository.deleteAll(stepsToDelete);
    }

    /**
     * Create a map of current steps with their IDs as keys.
     *
     * @param currentSteps Set of current steps.
     * @return Map of current steps with IDs as keys.
     */
    private Map<UUID, LearningLibraryStep> createCurrentStepMap(Set<LearningLibraryStep> currentSteps) {
        if (ObjectUtils.isEmpty(currentSteps)) {
            return new HashMap<>();
        }
        return currentSteps.stream()
            .collect(Collectors.toMap(LearningLibraryStep::getId, Function.identity()));
    }

    /**
     * Get an existing step by ID or create a new one if it doesn't exist.
     *
     * @param request        RequestLearningLibraryStep object.
     * @param currentStepMap Map of current steps.
     * @return LearningLibraryStep object.
     */
    private LearningLibraryStep updateOrCreateStep(RequestLearningLibraryStep request, Map<UUID, LearningLibraryStep> currentStepMap) {
        if (Objects.nonNull(request.getId())) {
            var stepUpdate = currentStepMap.get(UUID.fromString(request.getId()));
            learningLibraryStepMapper.partialEntity(stepUpdate, request);
            return stepUpdate;
        }
        return learningLibraryStepMapper.toEntity(request);
    }

    /**
     * Save data learning library lesson
     *
     * @param learningLibraryStep    LearningLibraryStep
     * @param learningLibraryLessons List<RequestLearningLibraryLesson>
     */
    private void saveDataLearningLibraryLesson(LearningLibraryStep learningLibraryStep, List<RequestLearningLibraryLesson> learningLibraryLessons) {
        Set<LearningLibraryLesson> currentLessons = learningLibraryStep.getLearningLibraryLessons();

        if (ObjectUtils.isEmpty(learningLibraryLessons)) {
            learningLibraryLessonRepository.deleteAll(currentLessons);
            return;
        }

        Map<UUID, LearningLibraryLesson> currentLessonMap = createCurrentLessonMap(currentLessons);
        List<LearningLibraryLesson> lessonsToDelete = new ArrayList<>(currentLessons);

        for (RequestLearningLibraryLesson request : learningLibraryLessons) {
            LearningLibraryLesson learningLibraryLesson = updateOrCreateLesson(request, currentLessonMap);
            if (Objects.nonNull(learningLibraryLesson.getId())) {
                lessonsToDelete.remove(learningLibraryLesson);
            }
            learningLibraryLesson.setLearningLibraryStep(learningLibraryStep);
            learningLibraryLesson = learningLibraryLessonRepository.save(learningLibraryLesson);
            saveDataLessonSection(learningLibraryLesson, request.getSections());

        }
        learningLibraryLessonRepository.deleteAll(lessonsToDelete);
    }

    /**
     * Create a map of current lessons with their IDs as keys.
     *
     * @param currentLessons Set of current lessons.
     * @return Map of current lessons with IDs as keys.
     */
    private Map<UUID, LearningLibraryLesson> createCurrentLessonMap(Set<LearningLibraryLesson> currentLessons) {
        if (ObjectUtils.isEmpty(currentLessons)) {
            return new HashMap<>();
        }
        return currentLessons.stream()
            .collect(Collectors.toMap(LearningLibraryLesson::getId, Function.identity()));
    }

    /**
     * Get an existing lesson by ID or create a new one if it doesn't exist.
     *
     * @param request          RequestLearningLibraryLesson object.
     * @param currentLessonMap Map of current lessons.
     * @return LearningLibraryLesson object.
     */
    private LearningLibraryLesson updateOrCreateLesson(RequestLearningLibraryLesson request, Map<UUID, LearningLibraryLesson> currentLessonMap) {
        if (Objects.nonNull(request.getId())) {
            var lessonUpdate = currentLessonMap.get(UUID.fromString(request.getId()));
            learningLibraryLessonMapper.partialEntity(lessonUpdate, request);
            return lessonUpdate;
        }
        return learningLibraryLessonMapper.toEntity(request);
    }

    /**
     * save Data Lesson Section
     *
     * @param lesson          LearningLibraryLesson
     * @param sectionRequests List<RequestLearningLibrarySection>
     */
    private void saveDataLessonSection(LearningLibraryLesson lesson, List<RequestLearningLibrarySection> sectionRequests) {
        validateSection(sectionRequests);
        Set<LearningLibrarySection> currentSections = lesson.getSections();
        if (ObjectUtils.isEmpty(sectionRequests)) {
            learningLibrarySectionRepository.deleteAll(currentSections);
            return;
        }

        Map<UUID, LearningLibrarySection> currentSectionMap = createCurrentSectionMap(currentSections);
        List<LearningLibrarySection> sectionsToDelete = new ArrayList<>(currentSections);
        List<LearningLibrarySection> sectionsToSave = new ArrayList<>();

        for (RequestLearningLibrarySection request : sectionRequests) {
            LearningLibrarySection section = updateOrCreateSection(request, currentSectionMap);
            if (Objects.nonNull(section.getId())) {
                sectionsToDelete.remove(section);
            }
            section.setLearningLibraryLesson(lesson);
            section.setContents(jsonElementService.convertDtoToJsonElement(request));
            if (SectionTypeEnum.SURVEY.equals(section.getSectionType())) {
                extractSurveyTitleAndDescription(section, request);
            }

            sectionsToSave.add(section);
        }
        learningLibrarySectionRepository.saveAll(sectionsToSave);
        learningLibrarySectionRepository.deleteAll(sectionsToDelete);
    }

    /**
     * Extract survey Title And Description
     *
     * @param section LearningLibrarySection
     * @param request RequestLearningLibrarySection
     *
     */
    private void extractSurveyTitleAndDescription(LearningLibrarySection section, RequestLearningLibrarySection request) {
        try {
            JsonObject jsonObject = JsonParser.parseString(request.getSurveyContent()).getAsJsonObject();

            String title = jsonObject.has("title") && !jsonObject.get("title").isJsonNull()
                ? jsonObject.get("title").getAsString().trim()
                : null;

            String description = jsonObject.has("description") && !jsonObject.get("description").isJsonNull()
                ? jsonObject.get("description").getAsString().trim()
                : null;

            section.setSurveyTitle(title);
            section.setSurveyDescription(description);
        } catch (JsonSyntaxException e) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0020, "JSON format"), e);
        }
    }

    /**
     * Get Attribute AsString
     *
     * @param jsonObject JsonObject
     * @param memberName String
     * @return String
     */
    private String getAsString(JsonObject jsonObject, String memberName) {
        return jsonObject.has(memberName) && !jsonObject.get(memberName).isJsonNull()
            ? jsonObject.get(memberName).getAsString()
            : "";
    }

    /**
     * Create a map of current sections with their IDs as keys.
     *
     * @param currentSections Set of current sections.
     * @return Map of current sections with IDs as keys.
     */
    private Map<UUID, LearningLibrarySection> createCurrentSectionMap(Set<LearningLibrarySection> currentSections) {
        if (ObjectUtils.isEmpty(currentSections)) {
            return new HashMap<>();
        }
        return currentSections.stream()
            .collect(Collectors.toMap(LearningLibrarySection::getId, Function.identity()));
    }

    /**
     * Get an existing section by ID or create a new one if it doesn't exist.
     *
     * @param request           RequestLearningLibrarySection object.
     * @param currentSectionMap Map of current sections.
     * @return LearningLibrarySection object.
     */
    private LearningLibrarySection updateOrCreateSection(RequestLearningLibrarySection request, Map<UUID, LearningLibrarySection> currentSectionMap) {
        if (Objects.nonNull(request.getId())) {
            var sectionUpdate = currentSectionMap.get(request.getId());
            learningLibrarySectionMapper.partialEntity(sectionUpdate, request);
            return sectionUpdate;
        }
        return learningLibrarySectionMapper.toEntity(request);
    }

    /**
     * Rating LearningLibrary
     *
     * @param request RequestRatingLearningLibrary
     */
    public void ratingLearningLibrary(RequestRatingLearningLibrary request) {
        UUID learningLibraryId = request.getLearningLibraryId();
        User user = getCurrentUser();

        UserLearningLibrary userLearningLibrary = getUserLearningLibrary(user.getId(), learningLibraryId)
            .map(ull -> {
                ull.setRating(request.getRating());
                return ull;
            })
            .orElseGet(() -> {
                LearningLibrary learningLibrary = getLearningLibrary(learningLibraryId);
                return userLearningLibraryMapper.toEntityRating(user, learningLibrary, request.getRating());
            });

        userLearningLibraryRepository.save(userLearningLibrary);
    }

    /**
     * Validate Section of Lesson
     *
     * @param sectionRequests List<RequestLearningLibrarySection>
     */
    private void validateSection(List<RequestLearningLibrarySection> sectionRequests) {
        validateSurveyFormSection(sectionRequests);

        sectionRequests.forEach(section -> {
            switch (section.getSectionType()) {
                case TEXT -> validateTextContent(section);
                case IMAGE -> validateImagePath(section);
                case AUDIO -> validateAudioUrl(section);
                case VIDEO -> validateVideoUrl(section);
                case FILES -> validateSectionFiles(section);
                case SURVEY -> validateSurveyContent(section);
            }
        });
    }

    /**
     * Validate Section Video Type
     *
     * @param content RequestSectionContent
     */
    private void validateVideoUrl(RequestLearningLibrarySection content) {
        validateUrl(content.getMediaUrl(), AppConstants.YOUTUBE_OR_VIMEO_URL_REGEX, "YouTube or Vimeo URL", "YouTube or Vimeo");
    }

    /**
     * Validate Section Image Type
     *
     * @param content RequestSectionContent
     */
    private void validateImagePath(RequestLearningLibrarySection content) {
        validateNotEmpty(content.getMediaUrl(), "Image Uploader");
    }

    /**
     * Validate Section File Type
     *
     * @param content RequestSectionContent
     */
    private void validateSectionFiles(RequestLearningLibrarySection content) {
        validateNotEmpty(content.getSectionFiles(), "File Upload");
        validateSize(content.getSectionFiles().size(), MAX_SECTION_FILES, "Section Files");
    }


    /**
     * Validate Section Audio Type
     *
     * @param content RequestSectionContent
     */
    private void validateAudioUrl(RequestLearningLibrarySection content) {
        validateUrl(content.getMediaUrl(), AppConstants.SPOTIFY_URL_REGEX, "Spotify URL", "Spotify");
    }

    /**
     * Validate Section Text Type
     *
     * @param content RequestSectionContent
     */
    private void validateTextContent(RequestLearningLibrarySection content) {
        validateNotEmpty(content.getTextContent(), "Text content");
    }

    /**
     * Validate SurveyForm Section
     *
     * @param sectionRequests List<RequestLearningLibrarySection>
     */
    private void validateSurveyFormSection(List<RequestLearningLibrarySection> sectionRequests) {
        long surveyFormCount = sectionRequests.stream()
            .filter(section -> SectionTypeEnum.SURVEY.equals(section.getSectionType()))
            .count();

        if (surveyFormCount > 1) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0050));
        }
    }

    /**
     * Validate SurveyContent
     *
     * @param content RequestLearningLibrarySection
     */
    private void validateSurveyContent(RequestLearningLibrarySection content) {
        validateNotEmpty(content.getSurveyContent(), "Survey Form");
    }

    /**
     * Validate if content is not empty
     *
     * @param content   Object to validate
     * @param fieldName Name of the field being validated
     */
    private void validateNotEmpty(Object content, String fieldName) {
        if (ObjectUtils.isEmpty(content)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0016, fieldName));
        }
    }

    /**
     * @param currentSize current size request
     * @param maxSize     max size
     */
    private void validateSize(Integer currentSize, Integer maxSize, String fieldName) {
        if (currentSize > maxSize) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0038, fieldName, maxSize));
        }
    }

    /**
     * Validate if URL is valid based on regex pattern
     *
     * @param url                 URL to validate
     * @param regexPattern        Regex pattern to match URL
     * @param emptyMessageField   Field name for empty URL message
     * @param invalidMessageField Field name for invalid URL message
     */
    private void validateUrl(String url, String regexPattern, String emptyMessageField, String invalidMessageField) {
        if (ObjectUtils.isEmpty(url)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0016, emptyMessageField));
        }
        var isUrlValid = Pattern.compile(regexPattern).matcher(url).matches();
        if (!isUrlValid) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0037, invalidMessageField));
        }
    }

    /**
     * Get current User
     *
     * @return User
     */
    private User getCurrentUser() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin).orElse(null);
    }
}
