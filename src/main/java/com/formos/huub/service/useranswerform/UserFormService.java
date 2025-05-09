package com.formos.huub.service.useranswerform;

import com.formos.huub.domain.constant.FormConstant;
import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.*;
import com.formos.huub.domain.request.portals.RequestSubmitTechnicalAssistance;
import com.formos.huub.domain.request.useranswerform.RequestAnswerDemoGraphics;
import com.formos.huub.domain.request.useranswerform.RequestAnswerForm;
import com.formos.huub.domain.request.useranswerform.RequestUserAnswerForm;
import com.formos.huub.domain.response.answerform.*;
import com.formos.huub.domain.response.member.ResponseBusinessOwnerQuestionForm;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.helper.member.MemberHelper;
import com.formos.huub.helper.portal.PortalHelper;
import com.formos.huub.mapper.useranswerform.AnswerOptionMapper;
import com.formos.huub.mapper.useranswerform.QuestionMapper;
import com.formos.huub.mapper.useranswerform.UserAnswerFormMapper;
import com.formos.huub.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class UserFormService {

    UserAnswerFormRepository userAnswerFormRepository;

    AnswerOptionMapper answerOptionMapper;

    UserAnswerFormMapper userAnswerFormMapper;

    UserAnswerOptionRepository userAnswerOptionRepository;

    QuestionRepository questionRepository;

    QuestionMapper questionMapper;

    AnswerOptionRepository answerOptionRepository;

    LanguageRepository languageRepository;

    PortalHelper portalHelper;

    TechnicalAdvisorRepository technicalAdvisorRepository;

    CommunityPartnerRepository communityPartnerRepository;

    CategoryRepository categoryRepository;

    TechnicalAssistanceSubmitRepository technicalAssistanceSubmitRepository;

    PortalFeatureRepository portalFeatureRepository;

    LocationRepository locationRepository;

    ServiceOfferedRepository serviceOfferedRepository;

    MemberHelper memberHelper;

    UserRepository userRepository;

    private static final String OTHER = "OTHER";

    private static final String KEY_STEP = "step%s";

    /**
     * get All Answer By Form Code
     *
     * @param formCode String
     * @return List<ResponseAnswer>
     */
    public List<ResponseQuestionForm> getAllAnswerByForm(String formCode) {
        var questions = questionRepository
            .getAllByFormCodeOrderByPriorityOrder(FormCodeEnum.valueOf(formCode));
        return toResponseQuestions(questions);
    }

    /**
     * get All Answer By Form Code And PortalId
     *
     * @param formCode String
     * @return List<ResponseAnswer>
     */
    public List<ResponseQuestionForm> getAllAnswerByFormAndPortalId(UUID portalId, String formCode) {
        var questions = questionRepository
            .getAllByPortalAndFormCode(portalId, FormCodeEnum.valueOf(formCode));
        return toResponseQuestions(questions);
    }

    /**
     * get All Questions By Code In
     *
     * @param questionCodes String list
     * @return List<ResponseQuestionForm>
     */
    public List<ResponseQuestionForm> getAllQuestionsByCodeIn(List<String> questionCodes, UUID portalId) {
        var otherOption = answerOptionRepository.findById(FormConstant.ANSWER_OTHER_ID);
        ResponseAnswer otherOptionResponse;
        if (otherOption.isPresent()) {
            otherOptionResponse = mapToResponseAnswer(otherOption.get());
            otherOptionResponse.setIsOther(true);
        } else {
            otherOptionResponse = null;
        }
        // Fetch questions by codes and map to ResponseQuestionForm
        return questionRepository.getAllConfigByQuestionCodesAndPortalId(questionCodes, portalId).stream()
            .map(question -> mapToResponseQuestionForm(question, otherOptionResponse, portalId))
            .toList();
    }

    /**
     * get All Steps Form
     *
     * @param portalId UUID
     * @return Map<String, List < ResponseQuestionForm>>
     */
    public Map<String, List<ResponseQuestionForm>> getAllStepsForm(UUID portalId) {
        var steps = new LinkedList<>(FormConstant.LIST_STEPS_BUSINESS_OWNER_REGISTER_FORM);
        var isEnableFeatureProgram = enablePortalFeature(portalId);
        if (isEnableFeatureProgram) {
            steps.add(FormConstant.LIST_QUESTION_BUSINESS_OWNER_REGISTER_STEP_7);
        }
        var stepMap = IntStream.range(0, steps.size())
            .boxed()
            .collect(Collectors.toMap(
                index -> String.format(KEY_STEP, index + 1),
                index -> getQuestionsInStep(steps.get(index), portalId)
            ));
        var additionalQuestions = getAllAnswerByFormAndPortalId(portalId, FormCodeEnum.PORTAL_INTAKE_ADDITIONAL_QUESTION.getValue());
        if (!ObjectUtils.isEmpty(additionalQuestions)) {
            stepMap.put(String.format(KEY_STEP, 8), additionalQuestions);
        }
        return stepMap;
    }

    /**
     * enable Portal Feature
     *
     * @param portalId UUID
     * @return Boolean
     */
    private Boolean enablePortalFeature(UUID portalId) {
        var portalFeature = portalFeatureRepository.findById_Feature_FeatureCodeAndId_Portal_Id(FeatureCodeEnum.PROGRAM_DETAILS, portalId);
        if (portalFeature.isPresent()) {
            return portalFeature.get().getIsActive();
        }
        return false;
    }

    /**
     * get Questions In Step
     *
     * @param questionCodes List<String>
     * @return List<ResponseQuestionForm>
     */
    private List<ResponseQuestionForm> getQuestionsInStep(List<String> questionCodes, UUID portalId) {
        return getAllQuestionsByCodeIn(questionCodes, portalId);
    }


    /**
     * map To Response Question Form
     *
     * @param question    IResponseQuestion
     * @param otherOption Optional<AnswerOption>
     * @return ResponseQuestionForm
     */
    private ResponseQuestionForm mapToResponseQuestionForm(IResponseQuestion question, ResponseAnswer otherOption, UUID portalId) {
        ResponseQuestionForm response = questionMapper.toResponseFromInterface(question);

        // Fetch answers for the question
        List<ResponseAnswer> answers = new ArrayList<>(getAnswerOptionByQuestion(question.getId(), question.getOptionType(), portalId));

        // If "Allow Other Input" is enabled and the "Other" option is present, add it to the answers
        if (Objects.nonNull(otherOption) && Boolean.TRUE.equals(question.getAllowOtherInput())) {
            answers.add(otherOption);
        }
        response.setRelatedQuestionCode(FormConstant.RELATED_QUESTION_CODE_MAP.get(question.getQuestionCode()));
        response.setAnswers(answers);
        return response;
    }

    /**
     * fill Form Demo graphics
     *
     * @param request RequestAnswerDemoGraphics
     */
    public void fillFormDemographics(RequestAnswerDemoGraphics request) {
        var technicalAdvisor = getTechnicalAdvisor(UUID.fromString(request.getTechnicalAdvisorId()));
        var user = technicalAdvisor.getUser();
        validateRequest(request.getAnswerForms(), FormConstant.LIST_QUESTION_DEMOGRAPHICS, FormConstant.LIST_QUESTION_DEMOGRAPHICS_REQUIRES);
        var oldAnswers = userAnswerFormRepository.getAllByEntryIdAndFormCode(user.getId(), EntryTypeEnum.USER, FormCodeEnum.DEMOGRAPHICS);
        userAnswerFormRepository.deleteAll(oldAnswers);
        saveNewAnswers(request.getAnswerForms(), user.getId(), EntryTypeEnum.USER);
    }

    /**
     * fill Form Demo graphics
     *
     * @param request RequestAnswerDemoGraphics
     */
    public void fillFormAboutScreenConfiguration(UUID portalId, RequestUserAnswerForm request) {
        var portal = portalHelper.getPortal(portalId);
        validateRequest(request.getAnswerForms(), FormConstant.LIST_QUESTION_ABOUT_SCREEN_CONFIGURATION, FormConstant.LIST_QUESTION_ABOUT_SCREEN_CONFIGURATION_REQUIRES);
        var oldAnswers = userAnswerFormRepository.getAllByEntryIdAndFormCode(portal.getId(), EntryTypeEnum.PORTAL, FormCodeEnum.PORTAL_ABOUT_SCREEN_CONFIGURATION);
        userAnswerFormRepository.deleteAll(oldAnswers);
        saveNewAnswers(request.getAnswerForms(), portalId, EntryTypeEnum.PORTAL);
    }

    /**
     * save New Answers
     *
     * @param answerForms List<RequestAnswerForm>
     * @param entryId     entryId
     */
    public void saveNewAnswers(List<RequestAnswerForm> answerForms, UUID entryId, EntryTypeEnum entryType) {
        answerForms.forEach(ele -> {
            UserAnswerForm newAnswerForm = userAnswerFormMapper.toEntity(ele, FormCodeEnum.DEMOGRAPHICS, entryId, entryType);
            newAnswerForm = userAnswerFormRepository.saveAndFlush(newAnswerForm);
            saveAnswerOptions(ele, newAnswerForm);
        });
    }


    /**
     * get Answer Demographics By User
     *
     * @param technicalAdvisorId UUID
     * @return List<ResponseUserAnswerForm>
     */
    public List<ResponseUserAnswerForm> getAnswerDemographicsByUser(UUID technicalAdvisorId) {
        var user = getTechnicalAdvisor(technicalAdvisorId).getUser();
        var userAnswerForms = userAnswerFormRepository.getAllByEntryIdAndFormCode(user.getId(), EntryTypeEnum.USER, FormCodeEnum.DEMOGRAPHICS);
        return convertToResponseUserAnswerForms(userAnswerForms);
    }

    /**
     * get Answer About Screen Configuration By Portal
     *
     * @param portalId UUID
     * @return List<ResponseUserAnswerForm>
     */
    public List<ResponseUserAnswerForm> getAnswerAboutScreenConfigurationByPortal(UUID portalId) {
        var portal = portalHelper.getPortal(portalId);
        var userAnswerForms = userAnswerFormRepository.getAllByEntryIdAndFormCode(portal.getId(), EntryTypeEnum.PORTAL, FormCodeEnum.PORTAL_ABOUT_SCREEN_CONFIGURATION);
        if (ObjectUtils.isEmpty(userAnswerForms)) {
            userAnswerForms = userAnswerFormRepository.getAllByPortalIdAndFormDefaultData(FormCodeEnum.PORTAL_ABOUT_SCREEN_CONFIGURATION);
        }
        return convertToResponseUserAnswerForms(userAnswerForms);
    }

    private List<ResponseQuestionForm> toResponseQuestions(List<Question> questions) {
        if (ObjectUtils.isEmpty(questions)) {
            return List.of();
        }
        return questions.stream()
            .map(this::toResponseQuestionItem)
            .toList();
    }

    private ResponseQuestionForm toResponseQuestionItem(Question question) {
        var item = questionMapper.toResponse(question);
        item.setPortalIntakeQuestionId(question.getId());
        item.setAnswers(getAnswerOptionByQuestion(question.getId(), question.getOptionType(), null));
        return item;
    }

    private List<ResponseUserAnswerForm> convertToResponseUserAnswerForms(List<UserAnswerForm> userAnswerForms) {
        var questions = questionRepository.findAllById(userAnswerForms.stream().map(UserAnswerForm::getQuestionId).toList());
        var questionMap = ObjectUtils.convertToMap(
            questions,
            Question::getId
        );
        return userAnswerForms.stream()
            .map(form -> convertToResponseUserAnswerForm(form, questionMap.get(form.getQuestionId()), questionMap))
            .toList();
    }

    private ResponseUserAnswerForm convertToResponseUserAnswerForm(UserAnswerForm userAnswerForm, Question question, Map<UUID, Question> questionMap) {
        var response = userAnswerFormMapper.toResponse(userAnswerForm);
        response.setAnswerDate(userAnswerForm.getCreatedDate());
        var options = userAnswerForm.getUserAnswerOptions();
        if (!ObjectUtils.isEmpty(options)) {
            var answerOptions = options.stream()
                .sorted(Comparator.comparing(UserAnswerOption::getPriorityOrder))
                .map(option -> option.getOptionId().toString())
                .toList();
            response.setAnswerOptions(answerOptions);
        }
        if (Objects.nonNull(question)) {
            BeanUtils.copyProperties(question, response);
            response.setParentId(question.getParentId());
            var parentQuestion = questionMap.get(question.getParentId());
            if (Objects.nonNull(parentQuestion)) {
                response.setQuestion(parentQuestion.getQuestion().replace("?", " Other input"));
            }
        }
        return response;
    }

    private TechnicalAdvisor getTechnicalAdvisor(UUID technicalAdvisorId) {
        return technicalAdvisorRepository.findById(technicalAdvisorId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Technical Advisor")));
    }

    private void validateRequest(List<RequestAnswerForm> answerForms, List<String> questionCodes, List<String> requireQuestionCodes) {
        if (ObjectUtils.isEmpty(answerForms)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0032));
        }
        validateQuestionCodes(answerForms, questionCodes);
        validateRequiredQuestions(answerForms, requireQuestionCodes);
    }

    public void validateQuestionCodes(List<RequestAnswerForm> answerForms, List<String> questionCodes) {
        var itemNotCorrects = answerForms.stream()
            .filter(ele -> !questionCodes.contains(ele.getQuestionCode()))
            .toList();

        if (!ObjectUtils.isEmpty(itemNotCorrects)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0032));
        }
    }

    public void validateRequiredQuestions(List<RequestAnswerForm> answerForms, List<String> questionCodes) {
        // Fetch questions based on demographic codes and map them by question code
        questionCodes = questionCodes.stream().filter(ele -> !ele.contains(OTHER)).toList();
        Map<UUID, Question> questionMap = getQuestionsMappedByCode(questionCodes);

        // Define the types that require answer options
        List<QuestionTypeEnum> typesRequiringOptions = List.of(
            QuestionTypeEnum.DROPDOWN_MULTIPLE_CHOICE,
            QuestionTypeEnum.DROPDOWN_MULTIPLE_CHOICE_OTHER,
            QuestionTypeEnum.DROPDOWN_SINGLE_CHOICE,
            QuestionTypeEnum.DROPDOWN_SINGLE_CHOICE_OTHER,
            QuestionTypeEnum.RADIOBUTTON,
            QuestionTypeEnum.CHECKBOX
        );

        List<String> ignoreTypeOptions = List.of(
            FormConstant.PORTAL_INTAKE_QUESTION_COUNTRY,
            FormConstant.PORTAL_INTAKE_QUESTION_STATE_PROVINCE,
            FormConstant.PORTAL_INTAKE_QUESTION_CITY,
            FormConstant.PORTAL_INTAKE_QUESTION_ZIPCODE
        );

        // Validate each required demographic question

        for (Map.Entry<UUID, Question> entry : questionMap.entrySet()) {
            var value = entry.getValue();
            var answerForm = findAnswerForQuestion(answerForms, value.getId());
            if (answerForm.isEmpty()) {
                return;
            }
            validateAnswerExists(answerForm);
            var question = questionMap.get(value.getId());
            validateAnswerOptionsOrAdditionalAnswer(answerForm.get(), question, typesRequiringOptions, ignoreTypeOptions);
        }
    }

    public Map<String, String> getAnswerUserByQuestionCode(UUID userId, List<String> questionCodes) {
        var businessDetails = getAllAnswerByUserAndQuestionCodes(userId, questionCodes);
        var businessDetailMap = ObjectUtils.convertToMap(businessDetails, ResponseUserAnswerForm::getQuestionCode);
        Map<String, String> responseMap = new HashMap<>();
        businessDetailMap.forEach((key, value) -> responseMap.put(value.getQuestionCode(), extractAnswer(value, userId, true)));
        return responseMap;
    }

    public List<ResponseUserAnswerForm> getAnswerUserByEntryFormId(UUID userId, UUID entryFormId, boolean isAppendOtherValue) {
        return getAllAnswerByUserAndEntryFormId(userId, entryFormId).stream().peek(ele -> ele.setAnswer(extractAnswer(ele, userId, isAppendOtherValue))).toList();
    }

    public List<ResponseUserAnswerForm> getListAnswerUserByQuestionCode(UUID userId, List<String> questionCodes, boolean isAppendOtherValue) {
        return getAllAnswerByUserAndQuestionCodes(userId, questionCodes).stream().peek(ele -> ele.setAnswer(extractAnswer(ele, userId, isAppendOtherValue))).toList();
    }

    public List<ResponseUserAnswerForm> getListAnswerAdditionalQuestionUserByFormCode(UUID userId, UUID portalId, FormCodeEnum formCodeEnum, boolean isAppendOtherValue) {
        return getAllAnswerAdditionalQuestionOfUserAndPortalAndForm(userId, portalId, formCodeEnum)
            .stream()
            .peek(ele -> ele.setAnswer(extractAnswer(ele, userId, isAppendOtherValue))).toList();
    }

    private String extractAnswer(ResponseUserAnswerForm response, UUID userId, boolean isAppendOtherValue) {
        StringBuilder answerData = new StringBuilder();
        if (Objects.isNull(response.getOptionType()) || (!ObjectUtils.isEmpty(response.getAdditionalAnswer())
            && !List.of(OptionTypeEnum.STATE, OptionTypeEnum.COUNTRY).contains(response.getOptionType()))) {
            answerData.append(response.getAdditionalAnswer());
        }

        var options = response.getAnswerOptions();
        if (ObjectUtils.isEmpty(options)) {
            options = Collections.emptyList();
        }

        // Collect answers
        if (Objects.nonNull(response.getOptionType())) {
            switch (response.getOptionType()) {
                case CATEGORY -> categoryRepository.findAllById(ObjectUtils.convertToUUIDList(options))
                    .forEach(option -> appendAnswer(answerData, option.getName()));
                case LANGUAGE -> languageRepository.findAllById(ObjectUtils.convertToUUIDList(options))
                    .forEach(option -> appendAnswer(answerData, option.getName()));
                case SERVICE -> serviceOfferedRepository.findAllById(ObjectUtils.convertToUUIDList(options))
                    .forEach(option -> appendAnswer(answerData, option.getName()));
                case COMMUNITY_PARTNER -> communityPartnerRepository.findAllById(ObjectUtils.convertToUUIDList(options))
                    .forEach(option -> appendAnswer(answerData, option.getName()));
                case ANSWER_OPTION -> answerOptionRepository.getAllByIdIn(ObjectUtils.convertToUUIDList(options))
                    .forEach(option -> appendAnswer(answerData, option.getAnswer()));
                case COUNTRY -> {
                    if (!ObjectUtils.isEmpty(response.getAdditionalAnswer())) {
                        locationRepository.findAllByGeoNameIdAndLocationType(response.getAdditionalAnswer(), LocationTypeEnum.COUNTRY)
                            .ifPresent(option -> appendAnswer(answerData, option.getName()));
                    }
                }
                case STATE -> {
                    if (!ObjectUtils.isEmpty(response.getAdditionalAnswer())) {
                        locationRepository.findOneByCodeAndLocationType(response.getAdditionalAnswer(), LocationTypeEnum.STATE)
                            .ifPresent(option -> appendAnswer(answerData, option.getName()));
                    }
                }
            }
        }
        // Include 'other' answer if present
        options.stream()
            .filter(option -> FormConstant.ANSWER_OTHER_ID.toString().equals(option))
            .findFirst()
            .ifPresent(other -> appendOtherAnswer(answerData, response.getQuestionId(), userId, isAppendOtherValue));
        return answerData.toString();
    }

    private void appendAnswer(StringBuilder builder, String answer) {
        if (!builder.isEmpty()) {
            builder.append(",");
        }
        builder.append(answer);
    }

    private void appendOtherAnswer(StringBuilder builder, UUID questionId, UUID userId, boolean isAppendOtherValue) {
        if (!isAppendOtherValue && builder.toString().contains("Other")) {
            return;
        }
        userAnswerFormRepository
            .findAnswerOtherByQuestionIdAndEntryIdAndEntryType(questionId, userId, EntryTypeEnum.USER)
            .stream().max(Comparator.comparing(UserAnswerForm::getCreatedDate))
            .ifPresent(answer -> appendAnswer(builder, isAppendOtherValue ? answer.getAdditionalAnswer() : "Other"));
    }

    private void validateAnswerExists(Optional<RequestAnswerForm> answerFormOpt) {
        if (answerFormOpt.isEmpty() ||
            (ObjectUtils.isEmpty(answerFormOpt.get().getAdditionalAnswer())
                && ObjectUtils.isEmpty(answerFormOpt.get().getAnswerOptions()))) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0032));
        }
    }

    private void validateAnswerOptionsOrAdditionalAnswer(RequestAnswerForm answerForm, Question question, List<QuestionTypeEnum> typesRequiringOptions, List<String> ignoreTypeOptions) {
        boolean requiresOptions = typesRequiringOptions.contains(question.getQuestionType());

        if (requiresOptions && ObjectUtils.isEmpty(answerForm.getAnswerOptions())
            && !ignoreTypeOptions.contains(question.getQuestionCode())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0032));
        }

        if (!requiresOptions && ObjectUtils.isEmpty(answerForm.getAdditionalAnswer())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0032));
        }
    }

    private Map<UUID, Question> getQuestionsMappedByCode(List<String> questionCodes) {
        var questions = questionRepository.getAllByQuestionCodeIn(questionCodes);
        return ObjectUtils.convertToMap(questions, Question::getId);
    }

    private Optional<RequestAnswerForm> findAnswerForQuestion(List<RequestAnswerForm> answerForms, UUID questionId) {
        return answerForms.stream()
            .filter(item -> questionId.equals(item.getQuestionId()))
            .findFirst();
    }


    private void saveAnswerOptions(RequestAnswerForm ele, UserAnswerForm newAnswerForm) {
        var answerOptionRequest = ele.getAnswerOptions();
        if (!ObjectUtils.isEmpty(answerOptionRequest)) {
            AtomicInteger order = new AtomicInteger();
            var answerOptions = answerOptionRequest.stream()
                .filter(Objects::nonNull)
                .map(option -> createUserAnswerOption(option, ele.getOptionType(), newAnswerForm, order.getAndIncrement()))
                .collect(Collectors.toSet());
            userAnswerOptionRepository.saveAll(answerOptions);
        }
    }

    private UserAnswerOption createUserAnswerOption(String option, OptionTypeEnum optionType, UserAnswerForm newAnswerForm, int order) {
        var newOption = new UserAnswerOption();
        newOption.setOptionId(UUID.fromString(option));
        newOption.setOptionType(optionType);
        newOption.setUserAnswerForm(newAnswerForm);
        newOption.setPriorityOrder(order);
        return newOption;
    }

    /**
     * Get answer option by question
     *
     * @param questionId UUID
     * @param optionType OptionTypeEnum
     * @param portalId   UUID
     * @return List<ResponseAnswer>
     */
    public List<ResponseAnswer> getAnswerOptionByQuestion(UUID questionId, OptionTypeEnum optionType, UUID portalId) {
        if (Objects.isNull(optionType)) {
            return Collections.emptyList();
        }
        return switch (optionType) {
            case LANGUAGE -> getLanguageOptions();
            case ANSWER_OPTION, COUNTRY  -> getAnswerOptions(questionId, portalId);
            case COMMUNITY_PARTNER -> getCommunityPartner(questionId, portalId);
            case CATEGORY -> getCategories(questionId);
            default -> Collections.emptyList();
        };
    }


    private List<ResponseAnswer> getCommunityPartner(UUID questionId, UUID portalId) {
        return communityPartnerRepository.findAllByPortalsId(portalId).stream()
            .map(ele -> ResponseAnswer.builder()
                .id(ele.getId())
                .answer(ele.getName())
                .questionId(questionId)
                .build())
            .toList();
    }

    private List<ResponseAnswer> getCategories(UUID questionId) {
        return categoryRepository.findAll().stream()
            .map(ele -> ResponseAnswer.builder()
                .id(ele.getId())
                .answer(ele.getName())
                .questionId(questionId)
                .build())
            .toList();
    }

    private List<ResponseAnswer> getLanguageOptions() {
        return languageRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
            .map(language -> new ResponseAnswer(language.getId(), language.getName()))
            .sorted((Comparator.comparing(ResponseAnswer::getAnswer)))
            .toList();
    }

    private List<ResponseAnswer> getAnswerOptions(UUID questionId, UUID portalId) {
        return answerOptionRepository.getAllByQuestionAndPortalId(questionId, portalId).stream()
            .map(answerOptionMapper::toResponse)
            .sorted((Comparator.comparing(ResponseAnswer::getPriorityOrder)))
            .toList();
    }

    private ResponseAnswer mapToResponseAnswer(AnswerOption answerOption) {
        ResponseAnswer responseAnswer = answerOptionMapper.toResponse(answerOption);
        var question = answerOption.getQuestion();
        if (Objects.nonNull(question) && FormCodeEnum.PORTAL_INTAKE_ADDITIONAL_QUESTION.equals(question.getFormCode())) {
            responseAnswer.setIsExtra(true);
        }
        return responseAnswer;
    }

    public void fillFormBusinessOwner(UUID userId, List<RequestAnswerForm> requests, String displayForm) {
        var oldAnswers = userAnswerFormRepository.getAllBusinessOwnerAnswerByEntryIdAndEntryType(userId, EntryTypeEnum.USER, displayForm);
        userAnswerFormRepository.deleteAll(oldAnswers);
        saveNewAnswers(requests, userId, EntryTypeEnum.USER);

        // Handle update user info
        if (FormConstant.BUSINESS_OWNER_DETAILS_MODAL.equals(displayForm)) {
            saveUserInformation(requests, userId);
        }
    }

    /**
     * Save User Information
     *
     * @param requests List<RequestAnswerForm>
     * @param userId UUID
     */
    public void saveUserInformation(List<RequestAnswerForm> requests, UUID userId) {
        var answerMap = ObjectUtils.convertToMap(requests, RequestAnswerForm::getQuestionCode);
        String email = getAnswerValue(answerMap, FormConstant.PORTAL_INTAKE_QUESTION_EMAIL_ADDRESS);
        User user = memberHelper.getUserById(userId);
        user.setLogin(email);
        user.setEmail(email);
        user.setPhoneNumber(getAnswerValue(answerMap, FormConstant.PORTAL_INTAKE_QUESTION_PHONE_NUMBER));
        user.setCountry(getAnswerValue(answerMap, FormConstant.PORTAL_INTAKE_QUESTION_COUNTRY));
        user.setState(getAnswerValue(answerMap, FormConstant.PORTAL_INTAKE_QUESTION_STATE_PROVINCE));
        user.setCity(getAnswerValue(answerMap, FormConstant.PORTAL_INTAKE_QUESTION_CITY));
        user.setZipCode(getAnswerValue(answerMap, FormConstant.PORTAL_INTAKE_QUESTION_ZIPCODE));
        user.setAddress1(getAnswerValue(answerMap, FormConstant.PORTAL_INTAKE_QUESTION_STREET_ADDRESS));
        userRepository.save(user);
    }

    /**
     * Get Answer Value
     *
     * @param answerMap Map<String, RequestAnswerForm>
     * @param questionCode String
     * @return String
     */
    private String getAnswerValue(Map<String, RequestAnswerForm> answerMap, String questionCode) {
        return Optional.ofNullable(answerMap.get(questionCode)).map(RequestAnswerForm::getAdditionalAnswer).orElse(null);
    }

    public void fillTechnicalAssistanceFormBusinessOwner(UUID technicalAssistanceId, List<RequestAnswerForm> requests) {
        var technicalAssistanceSubmit = technicalAssistanceSubmitRepository.findById(technicalAssistanceId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Technical Assistance")));
        var businessOwnerUser = technicalAssistanceSubmit.getUser();
        var oldAnswers = userAnswerFormRepository.getAllByEntryIdAndEntryFormId(businessOwnerUser.getId(), technicalAssistanceId);
        userAnswerFormRepository.deleteAll(oldAnswers);
        var requestTa = new RequestSubmitTechnicalAssistance();
        requestTa.setAnswers(requests);
        submitTechnicalAssistanceApplication(businessOwnerUser, requestTa, technicalAssistanceSubmit.getId());
    }

    private void submitTechnicalAssistanceApplication(User user, RequestSubmitTechnicalAssistance request, UUID technicalAssistanceSubmissionId) {
        var answers = request.getAnswers().stream().peek(ele -> ele.setEntryFormId(technicalAssistanceSubmissionId)).toList();
        saveNewAnswers(answers, user.getId(), EntryTypeEnum.USER);
    }

    public List<ResponseUserAnswerForm> getAllBusinessOwnerAnswer(UUID userId, String displayForm) {
        var userAnswerForms = userAnswerFormRepository.getAllBusinessOwnerAnswerByEntryIdAndEntryType(userId, EntryTypeEnum.USER, displayForm);
        return convertToResponseUserAnswerForms(userAnswerForms);
    }

    public List<ResponseUserAnswerForm> getAllTechnicalAssistanceAnswer(UUID technicalAssistanceId) {
        var technicalAssistanceSubmit = technicalAssistanceSubmitRepository.findById(technicalAssistanceId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Technical Assistance")));
        var businessOwnerUser = technicalAssistanceSubmit.getUser();
        var userAnswerForms = userAnswerFormRepository.getAllByEntryIdAndEntryFormId(businessOwnerUser.getId(), technicalAssistanceId);
        return convertToResponseUserAnswerForms(userAnswerForms);
    }

    public List<ResponseUserAnswerForm> getAllAnswerByUserAndQuestionCodes(UUID userId, List<String> questionCodes) {
        var userAnswerForms = userAnswerFormRepository.getAllBusinessOwnerAnswerByEntryIdAndEntryTypeAndQuestionCode(userId, EntryTypeEnum.USER, questionCodes);
        return convertToResponseUserAnswerForms(userAnswerForms);
    }

    public List<ResponseUserAnswerForm> getAllAnswerAdditionalQuestionOfUserAndPortalAndForm(UUID userId, UUID portalId, FormCodeEnum formCode) {
        var userAnswerForms = userAnswerFormRepository.getAllAdditionalByUserAndPortalAndFormCode(userId, EntryTypeEnum.USER, portalId, formCode);
        return convertToResponseUserAnswerForms(userAnswerForms);
    }

    public List<ResponseUserAnswerForm> getAllAnswerByUserAndEntryFormId(UUID userId, UUID entryFormId) {
        var userAnswerForms = userAnswerFormRepository.getAllBusinessOwnerAnswerByEntryIdAndEntryTypeAndEntryFormId(userId, EntryTypeEnum.USER, entryFormId);
        return convertToResponseUserAnswerForms(userAnswerForms);
    }

    public ResponseBusinessOwnerQuestionForm getAllBusinessOwnerQuestionByPortal(UUID portalId, String displayForm) {
        ResponseBusinessOwnerQuestionForm response = new ResponseBusinessOwnerQuestionForm();
        response.setBusinessDetails(getBusinessOwnerQuestionByQuestionCodesAndPortalId(
            List.of(GroupCodeEnum.MEMBER_BUSINESS_OWNER_PROFILE, GroupCodeEnum.MEMBER_BUSINESS_OWNER_BUSINESS),
            portalId, displayForm));
        response.setDemographics(getBusinessOwnerQuestionByQuestionCodesAndPortalId(List.of(GroupCodeEnum.MEMBER_BUSINESS_OWNER_DEMOGRAPHICS), portalId, displayForm));
        response.setAssistanceNeeds(getBusinessOwnerQuestionByQuestionCodesAndPortalId(List.of(GroupCodeEnum.MEMBER_BUSINESS_OWNER_ASSISTANCE_NEEDS), portalId, displayForm));
        response.setPortalQuestions(getAllAnswerByFormAndPortalId(portalId, FormCodeEnum.PORTAL_INTAKE_ADDITIONAL_QUESTION.getValue()));

        return response;
    }

    public List<ResponseQuestionForm> getBusinessOwnerQuestionByQuestionCodesAndPortalId(List<GroupCodeEnum> memberGroupCode, UUID portalId, String displayForm) {
        Optional<AnswerOption> otherOption = answerOptionRepository.findById(FormConstant.ANSWER_OTHER_ID);
        ResponseAnswer otherOptionResponse;
        if (otherOption.isPresent()) {
            otherOptionResponse = mapToResponseAnswer(otherOption.get());
            otherOptionResponse.setIsOther(true);
        } else {
            otherOptionResponse = null;
        }
        return questionRepository
            .getAllBusinessOwnerQuestionByCodesAndPortalId(memberGroupCode, portalId, displayForm)
            .stream()
            .map(question -> mapToResponseQuestionForm(question, otherOptionResponse, portalId))
            .toList();
    }

    public IResponseProfileCompletionStatus getProfileCompletionForTechnicalAdvisor(UUID technicalAdvisorId) {
        var profileCompletion = technicalAdvisorRepository.getProfileCompletionForTechnicalAdvisor(technicalAdvisorId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Technical Advisor")));

        return profileCompletion;
    }

}
