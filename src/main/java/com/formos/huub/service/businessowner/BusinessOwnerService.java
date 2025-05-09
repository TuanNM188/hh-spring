package com.formos.huub.service.businessowner;

import com.formos.huub.domain.constant.ActiveCampaignConstant;
import com.formos.huub.domain.constant.BusinessConstant;
import com.formos.huub.domain.constant.FormConstant;
import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.*;
import com.formos.huub.domain.request.businessowner.*;
import com.formos.huub.domain.request.portals.RequestSubmitTechnicalAssistance;
import com.formos.huub.domain.request.useranswerform.RequestAnswerForm;
import com.formos.huub.domain.request.useranswerform.RequestAnswerUnsupportedLocation;
import com.formos.huub.domain.response.answerform.ResponseQuestionForm;
import com.formos.huub.domain.response.businessowner.*;
import com.formos.huub.domain.response.communitypartner.IResponseAssignAdvisor;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.DateUtils;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.helper.member.MemberHelper;
import com.formos.huub.repository.*;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.activecampaign.ActiveCampaignService;
import com.formos.huub.service.activecampaign.ActiveCampaignStrategy;
import com.formos.huub.service.invite.InviteService;
import com.formos.huub.service.learninglibraryregistration.LearningLibraryRegistrationService;
import com.formos.huub.service.portals.PortalFormService;
import com.formos.huub.service.user.UserHelper;
import com.formos.huub.service.useranswerform.UserFormService;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.security.RandomUtil;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class BusinessOwnerService {

    BusinessOwnerRepository businessOwnerRepository;

    PortalRepository portalRepository;

    UserFormService userFormService;

    AuthorityRepository authorityRepository;

    UserRepository userRepository;

    InviteService inviteService;

    AnswerOptionRepository answerOptionRepository;

    PortalActivityLogRepository portalActivityLogRepository;

    ActiveCampaignService activeCampaignService;

    LocationRepository locationRepository;

    UserHelper userHelper;

    PortalFormService portalFormService;

    LearningLibraryRegistrationService learningLibraryRegistrationService;

    CalendarEventRepository calendarEventRepository;

    LearningLibraryRegistrationRepository learningLibraryRegistrationRepository;

    SurveyResponsesRepository surveyResponsesRepository;

    ActiveCampaignStrategy activeCampaignStrategy;

    MemberHelper memberHelper;

    private static final String ACTIVE_TYPE = "BUSINESS-OWNER-REGISTRATION";

    /**
     * get Data question for Register Business Owner Form
     *
     * @param portalId UUID
     * @return ResponseBusinessOwnerRegisterForm
     */
    public ResponseBusinessOwnerRegisterForm getRegisterForm(UUID portalId) {
        var portal = getPortal(portalId);
        var steps = userFormService.getAllStepsForm(portalId);
        return ResponseBusinessOwnerRegisterForm.builder()
            .platformName(portal.getPlatformName())
            .primaryLogo(portal.getPrimaryLogo())
            .primaryColor(portal.getPrimaryColor())
            .secondaryColor(portal.getSecondaryColor())
            .portalId(portalId)
            .steps(steps)
            .numStep(steps.size())
            .build();
    }

    /**
     * check Exist Email
     *
     * @param email String
     * @return ResponseCheckExistEmail
     */
    public ResponseCheckExistEmail checkExistEmail(String email, String userId) {
        var isExist = userRepository.existsByLoginIgnoreCaseIncludeDeleted(email);
        if (Objects.nonNull(userId) && StringUtils.isNotBlank(userId)) {
            UUID userUUID = UUID.fromString(userId);
            User user = memberHelper.getUserById(userUUID);

            if (Objects.nonNull(user) && email.equalsIgnoreCase(user.getEmail())) {
                isExist = false;
            }
        }

        return ResponseCheckExistEmail.builder().isExist(isExist).build();
    }

    /**
     * Registers a new Business Owner for the given portal.
     * Validates the form answers, creates a new Business Owner user, and saves the answers.
     *
     * @param portalId the ID of the portal
     * @param request  the registration request containing the form answers
     */
    public ResponseBusinessOwner registerBusinessOwner(UUID portalId, RequestRegisterBusinessOwner request) {
        // Validate the input form
        validateRegisterForm(portalId, request.getAnswers());

        // Retrieve portal information
        Portal portal = getPortal(portalId);

        // Convert answers to a map for quick access by question code
        var answerMap = ObjectUtils.convertToMap(request.getAnswers(), RequestAnswerForm::getQuestionCode);

        // Create and save the new business owner
        User newUser = createAndSaveBusinessOwner(portal, answerMap);

        // Collect question codes related to technical assistance
        var questionCodeApplyAssistance = prepareTechnicalAssistanceQuestionCodes();

        // Retrieve additional technical assistance question IDs for the portal
        var additionalTechnicalAssistanceQuestions = getAdditionalTechnicalAssistanceQuestions(portalId);

        // Filter answers for business owner registration and technical assistance
        var businessOwnerAnswers = filterBusinessOwnerAnswers(
            request.getAnswers(),
            questionCodeApplyAssistance,
            additionalTechnicalAssistanceQuestions
        );
        var technicalAssistanceAnswers = filterTechnicalAssistanceAnswers(
            request.getAnswers(),
            questionCodeApplyAssistance,
            additionalTechnicalAssistanceQuestions
        );

        // Save answers for business owner
        userFormService.saveNewAnswers(businessOwnerAnswers, newUser.getId(), EntryTypeEnum.USER);

        // Save and process technical assistance answers if applicable
        saveApplyTechnicalAssistance(
            portalId,
            newUser,
            technicalAssistanceAnswers,
            answerMap.get(FormConstant.PORTAL_INTAKE_QUESTION_WOULD_TOU_LIKE_APPLY_FREE_ASSISTANCE)
        );
        // Build and return the response
        return ResponseBusinessOwner.builder()
            .userId(newUser.getId())
            .portalName(portal.getPlatformName())
            .firstName(newUser.getFirstName())
            .lastName(newUser.getLastName())
            .build();
    }

    /**
     * Handle ActiveCampaign Register Business Owner
     *
     * @param userId UUID
     */
    public void handleActiveCampaignRegister(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (Objects.isNull(user)) {
            return;
        }
        // Handle active campaign
        Map<String, String> answerFieldMap = userFormService.getAnswerUserByQuestionCode(user.getId(), FormConstant.LIST_INTAKE_QUESTION_SYNC_ACTIVE_CAMPAIGN);

        Map<String, String> fieldMapping = ActiveCampaignConstant.FIELD_BUSINESS_OWNER_REGISTER_MAP;

        Map<String, String> fieldValueMapping = new HashMap<>();
        // Add standard fields
        fieldMapping.forEach(
            (fieldKey, questionKey) -> fieldValueMapping.put(fieldKey, answerFieldMap.get(questionKey)));

        var portalContext = PortalContextHolder.getContext();
        if (Objects.nonNull(portalContext)) {
            fieldValueMapping.put(ActiveCampaignConstant.FIELD_PORTAL_NAME_V2, portalContext.getPlatformName());
            fieldValueMapping.put(ActiveCampaignConstant.FIELD_PORTAL_DOMAIN_V2, portalContext.getUrl());
        }

        activeCampaignStrategy.syncValueActiveCampaignAndAddContactTags(user, fieldValueMapping, ActiveCampaignConstant.TAG_HUUB_REGISTERED_V2);
    }

    /**
     * Handle ActiveCampaign Business Owner Visit Page
     *
     * @param fieldName String
     */
    public void handleActiveCampaignVisitPage(String fieldName) {
        User user = SecurityUtils.getCurrentUser(userRepository);
        activeCampaignStrategy.handleIncrementContactField(user, fieldName);
    }

    private List<String> prepareTechnicalAssistanceQuestionCodes() {
        var questionCodes = new ArrayList<String>();
        questionCodes.addAll(FormConstant.LIST_QUESTION_TECHNICAL_ASSISTANCE_APPLICATION_APPLY_STEP_1);
        questionCodes.addAll(FormConstant.LIST_QUESTION_TECHNICAL_ASSISTANCE_APPLICATION_APPLY_STEP_2);
        return questionCodes;
    }

    private List<UUID> getAdditionalTechnicalAssistanceQuestions(UUID portalId) {
        return userFormService
            .getAllAnswerByFormAndPortalId(portalId, FormCodeEnum.TECHNICAL_ASSISTANCE_ADDITIONAL_QUESTION.getValue())
            .stream()
            .map(ResponseQuestionForm::getPortalIntakeQuestionId)
            .toList();
    }

    private List<RequestAnswerForm> filterBusinessOwnerAnswers(
        List<RequestAnswerForm> allAnswers,
        List<String> questionCodeApplyAssistance,
        List<UUID> additionalQuestionIds
    ) {
        return allAnswers
            .stream()
            .filter(
                answer ->
                    !questionCodeApplyAssistance.contains(answer.getQuestionCode()) &&
                        !additionalQuestionIds.contains(answer.getQuestionId())
            )
            .toList();
    }

    private List<RequestAnswerForm> filterTechnicalAssistanceAnswers(
        List<RequestAnswerForm> allAnswers,
        List<String> questionCodeApplyAssistance,
        List<UUID> additionalQuestionIds
    ) {
        return allAnswers
            .stream()
            .filter(
                answer ->
                    questionCodeApplyAssistance.contains(answer.getQuestionCode()) || additionalQuestionIds.contains(answer.getQuestionId())
            )
            .toList();
    }

    private void saveApplyTechnicalAssistance(
        UUID portalId,
        User user,
        List<RequestAnswerForm> technicalAssistanceAnswers,
        RequestAnswerForm assistanceRequest
    ) {
        if (assistanceRequest == null || ObjectUtils.isEmpty(assistanceRequest.getAnswerOptions())) {
            return;
        }

        answerOptionRepository
            .findById(UUID.fromString(assistanceRequest.getAnswerOptions().getFirst()))
            .ifPresent(option -> {
                if (FormConstant.ANSWER_YES.equals(option.getAnswer()) && !ObjectUtils.isEmpty(technicalAssistanceAnswers)) {
                    submitTechnicalAssistanceApplication(portalId, user, technicalAssistanceAnswers);
                }
            });
    }

    private void submitTechnicalAssistanceApplication(UUID portalId, User user, List<RequestAnswerForm> answers) {
        var request = RequestSubmitTechnicalAssistance.builder().answers(answers).build();
        portalFormService.submitTechnicalAssistanceApplication(portalId, user, request);
    }

    /**
     * get Register Data By Key
     *
     * @param key String
     * @return String response data
     */
    public String getRegisterDataByKey(String key) {
        PortalActivityLog portalActivityLog = portalActivityLogRepository
            .findById(UUID.fromString(key))
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "Transfer data")));
        return portalActivityLog.getData();
    }

    /**
     * check Location Supported
     *
     * @param request RequestCheckPortalSupportedLocation
     * @return ResponseCheckLocationSupported
     */
    public ResponseCheckLocationSupported checkLocationSupported(RequestCheckPortalSupportedLocation request) {
        ResponseCheckLocationSupported response = ResponseCheckLocationSupported.builder().isSupported(false).build();
        boolean isLocationSupported = portalRepository.checkSupportingPortal(
            request.getPortalId(),
            request.getCity(),
            request.getState(),
            request.getZipCode()
        );
        if (isLocationSupported) {
            response.setIsSupported(true);
            return response;
        }
        return handleUnsupportedLocation(request);
    }

    /**
     * handle Unsupported Location
     *
     * @param request RequestCheckPortalSupportedLocation
     * @return ResponseCheckLocationSupported
     */
    private ResponseCheckLocationSupported handleUnsupportedLocation(RequestCheckPortalSupportedLocation request) {
        ResponseCheckLocationSupported response = ResponseCheckLocationSupported.builder().isSupported(false).build();

        var newPortalSupported = portalRepository
            .findPortalSupportingLocation(request.getCity(), request.getState(), request.getZipCode())
            .stream()
            .findFirst();

        if (newPortalSupported.isEmpty()) {
            saveActivityLog(request, ProblemTypeEnum.PORTAL_NOT_SUPPORTED);
            saveDataContactCampaignIfExistGetIt(request.getData());
            return response;
        }

        var portal = newPortalSupported.get();
        var activityLog = saveActivityLog(request, ProblemTypeEnum.UNSUPPORTED_PORTAL_SWITCH_TO_PORTAL_SUPPORTED);
        response.setPortalSupportedUrl(portal.getUrl());
        response.setTransferKey(activityLog.getId().toString());
        response.setNewPortalName(portal.getPlatformName());

        return response;
    }

    /**
     * save Activity Log
     *
     * @param request     Request Check Portal Supported Location
     * @param problemType ProblemTypeEnum
     * @return PortalActivityLog
     */
    private PortalActivityLog saveActivityLog(RequestCheckPortalSupportedLocation request, ProblemTypeEnum problemType) {
        var activityLog = buildPortalActivityLog(request.getPortalId(), request, problemType);
        return portalActivityLogRepository.save(activityLog);
    }

    /**
     * build Portal Activity Log
     *
     * @param portalId    UUID
     * @param request     RequestCheckPortalSupportedLocation
     * @param problemType ProblemTypeEnum
     * @return PortalActivityLog object
     */
    private PortalActivityLog buildPortalActivityLog(
        UUID portalId,
        RequestCheckPortalSupportedLocation request,
        ProblemTypeEnum problemType
    ) {
        var gson = new Gson();
        var data = gson.toJson(request);
        return PortalActivityLog.builder()
            .portalId(portalId)
            .activityType(PortalActivityTypeEnum.BUSINESS_OWNER_REGISTRATION)
            .data(data)
            .problemType(problemType)
            .problem(problemType.getName())
            .build();
    }

    /**
     * Validates the registration form by checking if the provided answers match the expected questions for the portal.
     *
     * @param portalId the ID of the portal
     * @param answers  the list of answers provided by the user
     * @throws BadRequestException if the answers are invalid or missing
     */
    private void validateRegisterForm(UUID portalId, List<RequestAnswerForm> answers) {
        if (ObjectUtils.isEmpty(answers)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0032));
        }
        List<String> questionCodes = userFormService
            .getAllStepsForm(portalId)
            .values()
            .stream()
            .flatMap(Collection::stream)
            .map(ResponseQuestionForm::getQuestionCode)
            .toList();

        userFormService.validateRequiredQuestions(answers, questionCodes);
    }

    /**
     * Creates and saves a new Business Owner user for the given portal and form answers.
     *
     * @param portal the portal for which the Business Owner is being registered
     * @return the newly created User entity
     * @throws BadRequestException if the user with the provided email already exists
     */
    private User createAndSaveBusinessOwner(Portal portal, Map<String, RequestAnswerForm> answerMap) {
        String email = getAnswerValue(answerMap, FormConstant.PORTAL_INTAKE_QUESTION_EMAIL_ADDRESS);

        checkIfUserExists(email);

        User newUser = createUser(answerMap, email);
        newUser = userRepository.save(newUser);
        insertUserSettings(newUser.getId());

        createBusinessOwner(portal, newUser);
        sendBusinessOwnerInvitation(newUser, portal);

        return newUser;
    }

    /**
     * Checks if a user with the given email already exists in the system.
     *
     * @param email the email to check for an existing user
     * @throws BadRequestException if a user with the given email already exists
     */
    private void checkIfUserExists(String email) {
        userRepository
            .findOneByLogin(email)
            .ifPresent(user -> {
                throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "email"));
            });
    }

    /**
     * Creates a new User entity based on the provided answers and email.
     *
     * @param answerMap the map of question codes to answers
     * @param email     the email address of the new user
     * @return the newly created User entity
     */
    private User createUser(Map<String, RequestAnswerForm> answerMap, String email) {
        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(AuthoritiesConstants.BUSINESS_OWNER).ifPresent(authorities::add);
        String encryptedPassword = userHelper.genRandomPassword();
        var firstName = getAnswerValue(answerMap, FormConstant.PORTAL_INTAKE_QUESTION_FIRST_NAME);
        var lastName = getAnswerValue(answerMap, FormConstant.PORTAL_INTAKE_QUESTION_LAST_NAME);
        return User.builder()
            .login(email)
            .password(encryptedPassword)
            .firstName(firstName)
            .lastName(lastName)
            .username(inviteService.generateUsername(firstName, lastName))
            .phoneNumber(getAnswerValue(answerMap, FormConstant.PORTAL_INTAKE_QUESTION_PHONE_NUMBER))
            .city(getAnswerValue(answerMap, FormConstant.PORTAL_INTAKE_QUESTION_CITY))
            .state(getAnswerValue(answerMap, FormConstant.PORTAL_INTAKE_QUESTION_STATE_PROVINCE))
            .zipCode(getAnswerValue(answerMap, FormConstant.PORTAL_INTAKE_QUESTION_ZIPCODE))
            .country(getAnswerValue(answerMap, FormConstant.PORTAL_INTAKE_QUESTION_COUNTRY))
            .address1(getAnswerValue(answerMap, FormConstant.PORTAL_INTAKE_QUESTION_STREET_ADDRESS))
            .email(email)
            .activated(true)
            .status(UserStatusEnum.PENDING_APPROVAL)
            .authorities(authorities)
            .resetKey(RandomUtil.generateResetKey())
            .resetDate(Instant.now().plus(BusinessConstant.NUMBER_90, ChronoUnit.DAYS))
            .build();
    }

    /**
     * Creates and saves a new BusinessOwner entity associated with the given portal and user.
     *
     * @param portal  the portal to associate with the Business Owner
     * @param newUser the user to be set as the Business Owner
     */
    private void createBusinessOwner(Portal portal, User newUser) {
        BusinessOwner businessOwner = BusinessOwner.builder().user(newUser).portal(portal).build();
        businessOwnerRepository.save(businessOwner);
    }

    /**
     * Sends an invitation email to the newly created Business Owner with a reset key.
     *
     * @param newUser the new Business Owner user
     */
    private void sendBusinessOwnerInvitation(User newUser, Portal portal) {
        String guestName = String.join(" ", newUser.getFirstName(), newUser.getLastName());
        inviteService.sendInvitationBusinessOwner(newUser.getEmail(), guestName, newUser.getResetKey(), ACTIVE_TYPE, portal);
    }

    /**
     * Retrieves the additional answer from the answer form by question code.
     *
     * @param answerMap    the map of question codes to answer forms
     * @param questionCode the question code to look up the answer for
     * @return the additional answer string
     */
    private String getAnswerValue(Map<String, RequestAnswerForm> answerMap, String questionCode) {
        return Optional.ofNullable(answerMap.get(questionCode)).map(RequestAnswerForm::getAdditionalAnswer).orElse(null);
    }

    /**
     * Get portal
     *
     * @param id UUID
     * @return Portal
     */
    private Portal getPortal(UUID id) {
        return portalRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Portal")));
    }

    /**
     * Insert User Settings
     *
     * @param userId UUID
     */
    public void insertUserSettings(UUID userId) {
        userRepository.insertUserSettings(userId);
    }

    public void saveDataContactCampaignIfExistGetIt(Object data) {
        if (Objects.isNull(data)) {
            log.warn("Input data is null, skipping contact campaign save");
            return;
        }

        Map<String, String> valueQuestionMap = getQuestionsFromData(data);
        if (valueQuestionMap.isEmpty()) {
            log.warn("No valid questions data found");
            return;
        }

        String email = valueQuestionMap.get(FormConstant.PORTAL_INTAKE_QUESTION_EMAIL_ADDRESS);
        if (StringUtils.isEmpty(email)) {
            log.warn("Email address is required but not provided");
            return;
        }
        syncDataContactNotSupportedLocation(valueQuestionMap);
    }

    private void syncDataContactNotSupportedLocation(Map<String, String> valueQuestionMap) {
        var fieldValues = genFieldForUnsupportedAddress(valueQuestionMap);
        var user =  User.builder()
            .email(valueQuestionMap.get(FormConstant.PORTAL_INTAKE_QUESTION_EMAIL_ADDRESS))
            .phoneNumber(valueQuestionMap.get(FormConstant.PORTAL_INTAKE_QUESTION_PHONE_NUMBER))
            .firstName(valueQuestionMap.get(FormConstant.PORTAL_INTAKE_QUESTION_FIRST_NAME))
            .lastName(valueQuestionMap.get(FormConstant.PORTAL_INTAKE_QUESTION_LAST_NAME))
            .build();
        activeCampaignStrategy.syncValueActiveCampaignAndAddContactTags(user, fieldValues, ActiveCampaignConstant.TAG_HUUB_UNSUPPORTED_ADDRESS_V2);
    }

    private Map<String, String> genFieldForUnsupportedAddress(Map<String, String> valueQuestionMap) {
        if (Objects.isNull(valueQuestionMap) || valueQuestionMap.isEmpty()) {
            return Collections.emptyMap();
        }
        var portalContext = PortalContextHolder.getContext();

        Map<String, String> fieldValueMapping = new HashMap<>();

        // Map of field constants to their corresponding question values
        Map<String, String> fieldMapping = ActiveCampaignConstant.FIELD_UNSUPPORTED_ADDRESS_MAP;
        // Add standard fields
        fieldMapping.forEach(
            (fieldKey, questionKey) -> fieldValueMapping.put(fieldKey, valueQuestionMap.get(questionKey)));

        // Add portal context fields if available
        if (Objects.nonNull(portalContext)) {
            fieldValueMapping.put(ActiveCampaignConstant.FIELD_PORTAL_NAME_V2, portalContext.getPlatformName());
            fieldValueMapping.put(ActiveCampaignConstant.FIELD_PORTAL_DOMAIN_V2, portalContext.getUrl());
        }
        return fieldValueMapping;
    }

    private String valueHearAboutUs(Map<String, String> valueQuestionMap) {
        if (ObjectUtils.isEmpty(valueQuestionMap)) {
            return null;
        }
        var primaryAnswer = valueQuestionMap.get(FormConstant.PORTAL_INTAKE_QUESTION_HEAR_ABOUT_PROGRAM);
        return ObjectUtils.isValidUUID(primaryAnswer)
            ? valueQuestionMap.get(FormConstant.PORTAL_INTAKE_QUESTION_HEAR_ABOUT_PROGRAM_OTHER)
            : primaryAnswer;
    }

    private String valueCountry(Map<String, String> valueQuestionMap) {
        if (ObjectUtils.isEmpty(valueQuestionMap)) {
            return null;
        }
        var countryId = valueQuestionMap.get(FormConstant.PORTAL_INTAKE_QUESTION_COUNTRY);
        return locationRepository.findAllByGeoNameIdAndLocationType(countryId, LocationTypeEnum.COUNTRY).orElse(new Location()).getName();
    }

    private Map<String, String> getQuestionsFromData(Object data) {
        if (Objects.isNull(data)) {
            return Collections.emptyMap();
        }
        List<RequestAnswerUnsupportedLocation> answerQuestions = convertObjectToData(data);
        return answerQuestions
            .stream()
            .flatMap(answer -> answer.getQuestions().stream())
            .filter(question -> Objects.nonNull(question.getAnswersSelected()))
            .map(this::processQuestionAnswer)
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (existing, replacement) -> replacement // In case of duplicates, keep the last value
                )
            );
    }

    private List<RequestAnswerUnsupportedLocation> convertObjectToData(Object data) {
        try {
            var gson = new Gson();
            Type listType = new TypeToken<ArrayList<RequestAnswerUnsupportedLocation>>() {
            }.getType();
            List<RequestAnswerUnsupportedLocation> answerQuestions = gson.fromJson(gson.toJson(data), listType);
            if (!ObjectUtils.isEmpty(answerQuestions)) {
                return answerQuestions;
            }
        } catch (JsonSyntaxException e) {
            log.error("Error parsing question data: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    private Map.Entry<String, String> processQuestionAnswer(RequestAnswerUnsupportedLocation.Question question) {
        String answerSelected = question.getAnswersSelected();

        if (ObjectUtils.isValidUUID(answerSelected)) {
            UUID answerId = UUID.fromString(answerSelected);
            if (!FormConstant.ANSWER_OTHER_ID.equals(answerId)) {
                answerSelected = answerOptionRepository.findById(answerId).map(AnswerOption::getAnswer).orElse(answerSelected);
            }
        }
        return new AbstractMap.SimpleEntry<>(question.getQuestionCode(), answerSelected);
    }

    public List<IResponseAssignAdvisor> getBusinessOwnerByPortalId(String portalId) {
        return businessOwnerRepository.getBusinessOwnerByPortalId(UUID.fromString(portalId));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> searchBusinessOwnerByConditions(RequestSearchBusinessOwner request, Pageable pageable) {
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        UUID communityPartnerId = null;
        UUID technicalAdvisorId = null;

        if (Objects.nonNull(currentUser)) {
            if (
                SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.COMMUNITY_PARTNER) &&
                    Objects.nonNull(currentUser.getCommunityPartner())
            ) {
                communityPartnerId = currentUser.getCommunityPartner().getId();
            } else if (
                SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.TECHNICAL_ADVISOR) &&
                    Objects.nonNull(currentUser.getTechnicalAdvisor()) &&
                    Objects.nonNull(currentUser.getTechnicalAdvisor().getCommunityPartner())
            ) {
                communityPartnerId = currentUser.getTechnicalAdvisor().getCommunityPartner().getId();
                technicalAdvisorId = currentUser.getTechnicalAdvisor().getId();
            } else if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.PORTAL_HOST)) {
                request.setPortalId(PortalContextHolder.getPortalId());
            }
        }
        request.setSearchKeyword(StringUtils.makeStringWithContain(request.getSearchKeyword()));
        request.setCommunityPartnerId(communityPartnerId);
        request.setTechnicalAdvisorId(technicalAdvisorId);

        return PageUtils.toPage(businessOwnerRepository.searchBusinessOwnerByConditions(request, pageable));
    }

    /**
     * get Info Business Owner
     *
     * @param userId UUID
     * @return ResponseInfoBusinessOwner
     */
    public ResponseInfoBusinessOwner getInfoBusinessOwner(UUID userId) {
        var businessOwner = businessOwnerRepository
            .findByUserId(userId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Business Owner")));

        var response = buildResponseInfoBusinessOwner(userId, businessOwner);

        BeanUtils.copyProperties(businessOwner.getUser(), response);
        response.setBusinessName(learningLibraryRegistrationService.getBusinessName(userId));
        response.setFullName(businessOwner.getUser().getNormalizedFullName());

        return response;
    }

    private ResponseInfoBusinessOwner buildResponseInfoBusinessOwner(UUID userId, BusinessOwner businessOwner) {
        var portal = businessOwner.getPortal();

        return ResponseInfoBusinessOwner.builder()
            .id(userId)
            .businessOwnerId(businessOwner.getId())
            .portalId(portal != null ? portal.getId() : null)
            .platformName(portal != null ? portal.getPlatformName() : null)
            .build();
    }

    /**
     * search event registrations
     *
     * @param request RequestEventRegistrations
     * @return Map<String, Object>
     */
    public Map<String, Object> searchEventRegistrations(String userId, RequestSearchEventRegistrations request) {
        var businessOwner = validateIsBusinessOwner(UUID.fromString(userId));
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "eventDate,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        var data = calendarEventRepository.searchEventRegistrations(businessOwner.getId(), request, pageable);
        return PageUtils.toPage(data);
    }

    /**
     * search course registrations
     *
     * @param request RequestSearchCourseRegistrations
     * @return Map<String, Object>
     */
    public Map<String, Object> searchCourseRegistrations(String userId, RequestSearchCourseRegistrations request) {
        UUID userUId = UUID.fromString(userId);
        validateIsBusinessOwner(userUId);
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "enrolledDate,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        HashMap<String, String> sortMap = new HashMap<>();
        sortMap.put(BusinessConstant.TIMEZONE_KEY, request.getTimezone());
        request.setSearchConditions(ObjectUtils.convertSortParams(request.getSearchConditions(), sortMap));
        var data = learningLibraryRegistrationRepository.searchCourseRegistrations(userUId, request, pageable);
        return PageUtils.toPage(data);
    }

    /**
     * search course surveys
     *
     * @param request RequestSearchCourseSurveys
     * @return Map<String, Object>
     */
    public Map<String, Object> searchCourseSurveys(String userId, RequestSearchCourseSurveys request) {
        UUID userUId = UUID.fromString(userId);
        validateIsBusinessOwner(userUId);
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "submissionDate,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        return PageUtils.toPage(learningLibraryRegistrationRepository.searchCourseSurveys(userUId, request, pageable));
    }

    /**
     * search surveys
     *
     * @param request RequestSearchBusinessOwnerSurveys
     * @return Map<String, Object>
     */
    public Map<String, Object> searchBusinessOwnerSurveys(String userId, RequestSearchBusinessOwnerSurveys request) {
        UUID userUId = UUID.fromString(userId);
        validateIsBusinessOwner(userUId);
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "submissionDate,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        return PageUtils.toPage(surveyResponsesRepository.searchSurveysResponsesByUserId(userUId, request, pageable));
    }

    public BusinessOwner validateIsBusinessOwner(UUID userId) {
        return businessOwnerRepository
            .findByUserId(userId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Business Owner")));
    }

}
