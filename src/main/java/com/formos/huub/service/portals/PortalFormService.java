package com.formos.huub.service.portals;

import com.formos.huub.config.ApplicationProperties;
import com.formos.huub.domain.constant.ActiveCampaignConstant;
import com.formos.huub.domain.constant.BusinessConstant;
import com.formos.huub.domain.constant.EmailTemplatePathsConstants;
import com.formos.huub.domain.constant.FormConstant;
import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.*;
import com.formos.huub.domain.request.portals.RequestPortalIntakeQuestion;
import com.formos.huub.domain.request.portals.RequestSubmitTechnicalAssistance;
import com.formos.huub.domain.request.useranswerform.RequestAdditionalQuestion;
import com.formos.huub.domain.request.useranswerform.RequestAnswerOption;
import com.formos.huub.domain.request.useranswerform.RequestQuestionForm;
import com.formos.huub.domain.response.answerform.ResponseAnswer;
import com.formos.huub.domain.response.answerform.ResponseQuestionForm;
import com.formos.huub.domain.response.portals.ResponseApplyTechnicalAssistance;
import com.formos.huub.domain.response.portals.ResponsePortalIntakeQuestion;
import com.formos.huub.domain.response.portals.ResponseSubmitTechnicalAssistance;
import com.formos.huub.framework.constant.AppConstants;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.exception.NotFoundException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.service.mail.IMailService;
import com.formos.huub.framework.service.storage.model.CloudProperties;
import com.formos.huub.framework.utils.DateUtils;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.helper.file.FileHelper;
import com.formos.huub.helper.portal.PortalHelper;
import com.formos.huub.mapper.portals.PortalIntakeQuestionMapper;
import com.formos.huub.mapper.useranswerform.AnswerOptionMapper;
import com.formos.huub.mapper.useranswerform.QuestionMapper;
import com.formos.huub.repository.*;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.activecampaign.ActiveCampaignStrategy;
import com.formos.huub.service.invite.InviteService;
import com.formos.huub.service.useranswerform.UserFormService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.formos.huub.framework.constant.AppConstants.*;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PortalFormService {

    private static final String SUPPORT_EMAIL = "supportEmail";
    private static final String SUPPORT_LIVE_CHAT = "supportLiveChat";
    private static final String USER_ID = "userId";

    AnswerOptionMapper answerOptionMapper;

    QuestionRepository questionRepository;

    QuestionMapper questionMapper;

    AnswerOptionRepository answerOptionRepository;

    PortalIntakeQuestionRepository portalIntakeQuestionRepository;

    PortalIntakeQuestionMapper portalIntakeQuestionMapper;

    UserFormService userFormService;

    PortalFeatureRepository portalFeatureRepository;

    CommunityPartnerRepository communityPartnerRepository;

    PortalHelper portalHelper;

    UserRepository userRepository;

    TechnicalAssistanceSubmitRepository technicalAssistanceSubmitRepository;

    ApplicationProperties applicationProperties;

    InviteService inviteService;

    IMailService mailService;

    PortalHostRepository portalHostRepository;

    ProgramRepository programRepository;

    ProgramTermRepository programTermRepository;

    ActiveCampaignStrategy activeCampaignStrategy;

    FileHelper fileHelper;

    /**
     * Get All Portal Question
     *
     * @param portalId UUID
     * @return ResponsePortalIntakeQuestion
     */
    public ResponsePortalIntakeQuestion getAllPortalQuestion(UUID portalId) {
        ResponsePortalIntakeQuestion response = new ResponsePortalIntakeQuestion();
        response.setIntakeQuestionProfile(getIntakeQuestionByFormCodeAndPortalId(FormCodeEnum.PORTAL_INTAKE_QUESTION_PROFILE, portalId, List.of()));
        response.setIntakeQuestionDemographic(getIntakeQuestionByFormCodeAndPortalId(FormCodeEnum.PORTAL_INTAKE_QUESTION_DEMOGRAPHICS, portalId, List.of()));
        response.setIntakeQuestionBusiness(getQuestionBusinessForm());
        response.setIntakeAdditionalQuestions(getAdditionalQuestionsByPortal(portalId, FormCodeEnum.PORTAL_INTAKE_ADDITIONAL_QUESTION));
        response.setIntakeTechnicalAssistanceAdditionalQuestions(getAdditionalQuestionsByPortal(portalId, FormCodeEnum.TECHNICAL_ASSISTANCE_ADDITIONAL_QUESTION));
        response.setIntakeQuestionAssistanceNeed(getIntakeQuestionByFormCodeAndPortalId(FormCodeEnum.PORTAL_INTAKE_QUESTION_ASSISTANCE_NEEDS,
            portalId, List.of(FormConstant.PORTAL_INTAKE_QUESTION_WOULD_TOU_LIKE_APPLY_FREE_ASSISTANCE)));
        response.setIntakeQuestionSupportApplication(getIntakeQuestionFormSupportApplication(portalId));
        return response;
    }


    /**
     * Get Intake Question by FormCode
     *
     * @param formCode FormCodeEnum
     * @param portalId UUID
     * @return List<ResponseQuestionForm>
     */
    public List<ResponseQuestionForm> getIntakeQuestionByFormCodeAndPortalId(FormCodeEnum formCode, UUID portalId, List<String> excludeQuestionCodes) {
        return questionRepository
            .getAllByFormCodeAndPortalId(formCode, portalId, excludeQuestionCodes)
            .stream()
            .map(questionResponse -> {
                ResponseQuestionForm item = questionMapper.toResponseFromInterface(questionResponse);
                item.setAnswers(userFormService.getAnswerOptionByQuestion(item.getId(), item.getOptionType(), portalId));
                return item;
            })
            .toList();
    }

    /**
     * Fill Intake Questions Form by Portal
     *
     * @param request RequestPortalIntakeQuestion
     * @return ResponsePortalIntakeQuestion
     */
    public ResponsePortalIntakeQuestion fillIntakeQuestionsFormByPortal(RequestPortalIntakeQuestion request) {
        UUID portalId = UUID.fromString(request.getPortalId());

        ResponsePortalIntakeQuestion response = new ResponsePortalIntakeQuestion();
        response.setIntakeQuestionProfile(handleSavePortalIntakeQuestion(request.getIntakeQuestionProfile(), portalId));
        response.setIntakeQuestionDemographic(handleSavePortalIntakeQuestion(request.getIntakeQuestionDemographic(), portalId));
        response.setIntakeQuestionAssistanceNeed(handleSavePortalIntakeQuestion(request.getIntakeQuestionAssistanceNeed(), portalId));
        response.setIntakeQuestionSupportApplication(handleSavePortalIntakeQuestion(request.getIntakeQuestionSupportApplication(), portalId));
        response.setIntakeAdditionalQuestions(handleSaveAdditionalQuestionForPortal(request.getIntakeAdditionalQuestions(), portalId, FormCodeEnum.PORTAL_INTAKE_ADDITIONAL_QUESTION));
        response.setIntakeTechnicalAssistanceAdditionalQuestions(handleSaveAdditionalQuestionForPortal(request.getIntakeTechnicalAssistanceAdditionalQuestions(), portalId, FormCodeEnum.TECHNICAL_ASSISTANCE_ADDITIONAL_QUESTION));
        return response;
    }

    /**
     * get Data question for Register Business Owner Form
     *
     * @param portalId UUID
     * @return ResponseBusinessOwnerRegisterForm
     */
    public ResponseApplyTechnicalAssistance getApplyTechnicalAssistanceForm(UUID portalId) {
        var portal = portalHelper.getPortal(portalId);
        var steps = getAllStepsTechnicalAssistanceForm(portalId);
        var applyStatus = getSubmitTechnicalAssistanceStatus(portal);
        UUID programTermId = Optional.ofNullable(getCurrentTerm(portalId)).map(ProgramTerm::getId).orElse(null);
        return ResponseApplyTechnicalAssistance.builder()
            .platformName(portal.getPlatformName())
            .primaryLogo(portal.getPrimaryLogo())
            .primaryColor(portal.getPrimaryColor())
            .secondaryColor(portal.getSecondaryColor())
            .portalId(portalId)
            .steps(steps)
            .numStep(steps.size())
            .programTermId(programTermId)
            .submitStatus(applyStatus)
            .build();
    }

    public ApprovalStatusEnum getSubmitTechnicalAssistanceStatus(Portal portal) {
        var currentUser = getCurrentUser();
        if (Objects.isNull(currentUser)) {
            return null;
        }
        var currentTerm = getCurrentTerm(portal.getId());
        if (Objects.isNull(currentTerm)) {
            return null;
        }
        return technicalAssistanceSubmitRepository.findByPortalIdAndUserIdAndProgramTermId(portal.getId(), currentUser.getId(), currentTerm.getId())
            .map(TechnicalAssistanceSubmit::getStatus)
            .orElse(null);
    }

    public TechnicalAssistanceSubmit getSubmitTechnicalAssistance(Portal portal) {
        var currentUser = getCurrentUser();
        if (Objects.isNull(portal) || Objects.isNull(currentUser)) {
            return null;
        }
        var currentTerm = getCurrentTerm(portal.getId());
        if (Objects.isNull(currentTerm)){
            return null;
        }
        return technicalAssistanceSubmitRepository.findByPortalIdAndUserIdAndProgramTermId(portal.getId(), currentUser.getId(), currentTerm.getId())
            .orElse(null);
    }

    public ResponseSubmitTechnicalAssistance userSubmitTechnicalAssistanceApplication(UUID portalId, RequestSubmitTechnicalAssistance request) {
        var currentUser = getCurrentUser();
        if (Objects.isNull(currentUser)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0027, "Business Owner"));
        }
        return submitTechnicalAssistanceApplication(portalId, currentUser, request);
    }

    public ResponseSubmitTechnicalAssistance submitTechnicalAssistanceApplication(UUID portalId, User user, RequestSubmitTechnicalAssistance request) {
        Portal portal = portalHelper.getPortal(portalId);
        validateTechnicalAssistanceSubmission(portal);
        var primaryPortalHost = portalHostRepository.findPortalHostPrimaryByPortal(portalId).orElse(null);
        var programTerm = getCurrentTerm(portal.getId());
        if (Objects.isNull(programTerm)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0027, "Program Term"));
        }
        UUID technicalAssistanceSubmissionId = saveTechnicalAssistanceSubmission(portal, user, primaryPortalHost, programTerm);
        var answers = request.getAnswers().stream().peek(ele -> ele.setEntryFormId(technicalAssistanceSubmissionId)).toList();
        userFormService.saveNewAnswers(answers, user.getId(), EntryTypeEnum.USER);
        return buildResponseBusinessOwner(portal, user, primaryPortalHost);
    }

    private void validateTechnicalAssistanceSubmission(Portal portal) {
        var submitStatus = getSubmitTechnicalAssistanceStatus(portal);
        if (ApprovalStatusEnum.VENDOR_ASSIGNED.equals(submitStatus) ||
            ApprovalStatusEnum.SUBMITTED.equals(submitStatus) || ApprovalStatusEnum.APPROVED.equals(submitStatus)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0054));
        }

    }

    private UUID saveTechnicalAssistanceSubmission(Portal portal, User user, User programManager, ProgramTerm programTerm) {
        TechnicalAssistanceSubmit technicalAssistanceSubmit = TechnicalAssistanceSubmit.builder()
            .user(user)
            .portal(portal)
            .status(ApprovalStatusEnum.SUBMITTED)
            .submitAt(Instant.now())
            .build();
        technicalAssistanceSubmit.setProgramTerm(programTerm);
        technicalAssistanceSubmit = technicalAssistanceSubmitRepository.save(technicalAssistanceSubmit);

        sendMailSubmitSuccessTechnicalAssistance(user.getEmail(), user.getNormalizedFullName(), portal);
        if (Objects.nonNull(programManager)) {
            sendMailNeedReviewTechnicalAssistance(programManager.getEmail(), programManager.getFirstName(), portal, user.getId());
        }
        syncValueActiveCampaignApplication(user);

        return technicalAssistanceSubmit.getId();
    }

    public ProgramTerm getCurrentTerm(UUID portalId) {
        return programRepository.findByPortalId(portalId)
            .filter(Program::getIsActive)
            .flatMap(program -> programTermRepository.findCurrentByProgramId(program.getId()))
            .orElse(null);
    }

    private void syncValueActiveCampaignApplication(User user) {
        Map<String, String> campaignValueMap = new HashMap<>();
        campaignValueMap.put(ActiveCampaignConstant.FIELD_TA_STATUS_V2, ActiveCampaignConstant.APPLIED);
        campaignValueMap.put(ActiveCampaignConstant.FIELD_APPLICATION_DATE_V2, DateUtils.convertInstantToString(Instant.now()));
        activeCampaignStrategy.syncValueActiveCampaignApplication(user, campaignValueMap);
    }

    private ResponseSubmitTechnicalAssistance buildResponseBusinessOwner(Portal portal, User user, User programManager) {
        var response = ResponseSubmitTechnicalAssistance.builder()
            .portalName(portal.getPlatformName())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .build();
        if (Objects.nonNull(programManager)) {
            response.setProgramManagerEmail(programManager.getEmail());
            response.setProgramManagerFirstName(programManager.getFirstName());
        }
        return response;
    }

    /**
     * get All Steps Form
     *
     * @param portalId UUID
     * @return Map<String, List < ResponseQuestionForm>>
     */
    private Map<String, List<ResponseQuestionForm>> getAllStepsTechnicalAssistanceForm(UUID portalId) {
        // Initialize steps and create step map
        var steps = new LinkedList<>(FormConstant.LIST_STEPS_TECHNICAL_ASSISTANCE_APPLICATION_APPLY_FORM);
        var stepMap = IntStream.range(0, steps.size())
            .boxed()
            .collect(Collectors.toMap(
                index -> String.format(FormConstant.KEY_STEP, index + 1),
                index -> getQuestionsInStep(steps.get(index), portalId)
            ));

        // Handle additional questions
        var additionalQuestions = userFormService.getAllAnswerByFormAndPortalId(portalId, FormCodeEnum.TECHNICAL_ASSISTANCE_ADDITIONAL_QUESTION.getValue());

        if (!ObjectUtils.isEmpty(additionalQuestions)) {
            if (additionalQuestions.size() > FormConstant.MAX_PORTAL_ADDITIONAL_QUESTION) {
                stepMap.put(formatStepKey(3), additionalQuestions.subList(0, FormConstant.MAX_PORTAL_ADDITIONAL_QUESTION));
                stepMap.put(formatStepKey(4), additionalQuestions.subList(FormConstant.MAX_PORTAL_ADDITIONAL_QUESTION, additionalQuestions.size()));
            } else {
                stepMap.put(formatStepKey(3), additionalQuestions);
            }
        }
        return stepMap;
    }

    private String formatStepKey(int stepNumber) {
        return String.format(FormConstant.KEY_STEP, stepNumber);
    }


    private List<ResponseQuestionForm> getQuestionsInStep(List<String> questionCodes, UUID portalId) {
        return userFormService.getAllQuestionsByCodeIn(questionCodes, portalId);
    }

    private List<ResponseQuestionForm> getIntakeQuestionFormSupportApplication(UUID portalId) {
        if (!enablePortalTechnicalAssistancePrograms(portalId)) {
            return List.of();
        }
        var excludeQuestions = new ArrayList<String>();
        if (!isValidPreferredOrganization(portalId)) {
            excludeQuestions.add(FormConstant.PORTAL_INTAKE_QUESTION_PREFERRED_ORGANIZATION_FOR_ASSISTANCE);
        }
        return getIntakeQuestionByFormCodeAndPortalId(FormCodeEnum.PORTAL_INTAKE_SUPPORT_APPLICATIONS,
            portalId, excludeQuestions);
    }


    /**
     * enable Portal Feature
     *
     * @param portalId UUID
     * @return Boolean
     */
    private Boolean enablePortalTechnicalAssistancePrograms(UUID portalId) {
        var portalFeature = portalFeatureRepository.findById_Feature_FeatureCodeAndId_Portal_Id(FeatureCodeEnum.PROGRAM_DETAILS, portalId);
        if (portalFeature.isPresent()) {
            return portalFeature.get().getIsActive();
        }
        return false;
    }

    private Boolean isValidPreferredOrganization(UUID portalId) {
        var numCommunityPartner = communityPartnerRepository.countByPortalId(portalId);
        return numCommunityPartner > BusinessConstant.NUMBER_1;
    }

    /**
     * get Question Business Form
     *
     * @return ResponsePortalBusinessForm
     */
    private List<ResponseQuestionForm> getQuestionBusinessForm() {
        return userFormService.getAllAnswerByForm(FormCodeEnum.PORTAL_INTAKE_QUESTION_BUSINESS.getValue());
    }

    /**
     * get All Additional question by portal
     *
     * @return ResponsePortalBusinessForm
     */
    private List<ResponseQuestionForm> getAdditionalQuestionsByPortal(UUID portalId, FormCodeEnum formCode) {
        return userFormService.getAllAnswerByFormAndPortalId(portalId, formCode.getValue());
    }


    /**
     * Handle save Portal Intake Question
     *
     * @param request  List<RequestAnswerForm>
     * @param portalId UUID
     */
    private List<ResponseQuestionForm> handleSavePortalIntakeQuestion(List<RequestQuestionForm> request, UUID portalId) {
        if (ObjectUtils.isEmpty(request)) {
            return List.of();
        }
        // Extract question IDs from the request once
        var questionIds = request.stream()
            .map(RequestQuestionForm::getId)
            .toList();
        // Get the current intake questions and map by questionId
        var portalIntakeQuestionMap = portalIntakeQuestionRepository.getAllByPortalIdAndQuestionIds(portalId, questionIds)
            .stream()
            .collect(Collectors.toMap(PortalIntakeQuestion::getQuestionId, Function.identity()));

        // Process each request form, handling extra options and updating or creating new questions
        List<PortalIntakeQuestion> portalIntakeQuestions = request.stream()
            .peek(answer -> handleExtraOption(portalId, answer.getId(), answer.getAnswers()))  // Process extra options inline
            .map(answer -> mapOrUpdatePortalIntakeQuestion(answer, portalId, portalIntakeQuestionMap))
            .toList();

        // Save all processed portal intake questions
        portalIntakeQuestionRepository.saveAll(portalIntakeQuestions);

        // Return the response by question codes
        return getResponseByQuestionCodes(request, portalId);
    }

    private PortalIntakeQuestion mapOrUpdatePortalIntakeQuestion(RequestQuestionForm answer, UUID portalId,
                                                                 Map<UUID, PortalIntakeQuestion> portalIntakeQuestionMap) {
        var portalIntakeQuestion = portalIntakeQuestionMap.get(answer.getId());
        if (Objects.nonNull(portalIntakeQuestion)) {
            portalIntakeQuestionMapper.partialEntity(portalIntakeQuestion, answer);
            return portalIntakeQuestion;
        }
        return portalIntakeQuestionMapper.toEntity(answer, portalId);
    }

    private List<ResponseQuestionForm> getResponseByQuestionCodes(List<RequestQuestionForm> request, UUID portalId) {
        // Extract question codes once and get the response
        return getIntakeQuestionByQuestionCodesAndPortalId(
            request.stream().map(RequestQuestionForm::getQuestionCode).toList(),
            portalId
        );
    }

    /**
     * @param questionCode List<String>
     * @param portalId     UUID
     * @return List<ResponseQuestionForm>
     */
    private List<ResponseQuestionForm> getIntakeQuestionByQuestionCodesAndPortalId(List<String> questionCode, UUID portalId) {
        return questionRepository
            .getAllByQuestionCodesAndPortalId(questionCode, portalId)
            .stream()
            .map(questionResponse -> {
                ResponseQuestionForm item = questionMapper.toResponseFromInterface(questionResponse);
                item.setAnswers(userFormService.getAnswerOptionByQuestion(item.getId(), item.getOptionType(), portalId));
                return item;
            })
            .toList();
    }

    /**
     * handle Save Additional Question For Portal
     *
     * @param requests List<RequestAdditionalQuestion>
     * @param portalId UUID
     */
    private List<ResponseQuestionForm> handleSaveAdditionalQuestionForPortal(List<RequestAdditionalQuestion> requests, UUID portalId, FormCodeEnum formCode) {
        validateAdditionalQuestion(requests, formCode);
        var currentQuestions = questionRepository.getAllByPortalAndFormCode(portalId, formCode);
        if (ObjectUtils.isEmpty(requests)) {
            questionRepository.deleteAllByIdIn(currentQuestions.stream().map(Question::getId).toList());
            return List.of();
        }
        Map<UUID, Question> currentQuestionMap = ObjectUtils.convertToMap(currentQuestions, Question::getId);
        List<Question> questionsToDelete = new ArrayList<>(currentQuestions);
        List<Question> questionsToSave = processQuestions(requests, currentQuestionMap, questionsToDelete, formCode);
        questionRepository.deleteAll(questionsToDelete);
        saveQuestionForPortal(portalId, questionsToSave, formCode);
        return getAdditionalQuestionsByPortal(portalId, formCode);
    }


    private List<Question> processQuestions(List<RequestAdditionalQuestion> requests, Map<UUID, Question> currentQuestionMap, List<Question> questionsToDelete, FormCodeEnum formCode) {
        AtomicInteger priorityOrder = new AtomicInteger(0);
        return requests.stream()
            .map(request -> {
                Question question = updateOrCreateQuestion(request, currentQuestionMap, formCode);
                updateQuestionAttributes(question, priorityOrder.getAndIncrement());
                if (Objects.nonNull(question.getId())) {
                    questionsToDelete.remove(question);
                }
                question = questionRepository.save(question);
                saveDataAnswerOptions(question, request.getAnswers());
                return question;
            })
            .collect(Collectors.toList());
    }

    /**
     * update Question Attributes
     * update Question Attributes
     *
     * @param question      Question
     * @param priorityOrder int
     */
    private void updateQuestionAttributes(Question question, int priorityOrder) {
        question.setPriorityOrder(priorityOrder);
        question.setIsRequire(true);
        question.setOptionType(OptionTypeEnum.ANSWER_OPTION);
        question.setAllowCustomOptions(true);
        question.setAllowActionVisible(false);
        question.setPlaceholder(getPlaceHolderQuestion(question.getQuestionType()));
    }

    private String getPlaceHolderQuestion(QuestionTypeEnum questionType) {
        return FormConstant.PLACEHOLDER_MAP.getOrDefault(questionType, StringUtils.EMPTY);
    }

    /**
     * save Question For Portal
     *
     * @param portalId  UUID
     * @param questions List<Question>
     */
    private void saveQuestionForPortal(UUID portalId, List<Question> questions, FormCodeEnum formCode) {

        var currentPortalIntakeQuestions = portalIntakeQuestionRepository.getAllByPortalIdAndFormCodes(portalId, formCode);
        var currentPortalIntakeQuestionMap = ObjectUtils.convertToMap(currentPortalIntakeQuestions, PortalIntakeQuestion::getId);
        List<PortalIntakeQuestion> questionsToDelete = new ArrayList<>(currentPortalIntakeQuestions);
        List<PortalIntakeQuestion> questionsToSave = new ArrayList<>();
        questions.forEach(ele -> {
            var portalIntakeQuestion = currentPortalIntakeQuestionMap.get(ele.getId());
            if (Objects.nonNull(portalIntakeQuestion)) {
                questionsToDelete.remove(portalIntakeQuestion);
            } else {
                questionsToSave.add(createPortalIntakeQuestion(portalId, ele));
            }
        });
        portalIntakeQuestionRepository.saveAll(questionsToSave);
        portalIntakeQuestionRepository.deleteAll(questionsToDelete);
    }

    /**
     * create Portal Intake Question
     *
     * @param portalId UUID
     * @param question Question
     * @return PortalIntakeQuestion
     */
    private PortalIntakeQuestion createPortalIntakeQuestion(UUID portalId, Question question) {
        return PortalIntakeQuestion.builder()
            .portalId(portalId)
            .priorityOrder(question.getPriorityOrder())
            .columnSize(FormConstant.COLUMN_SIZE_12)
            .isVisible(true)
            .allowOtherInput(false)
            .questionId(question.getId())
            .build();
    }


    /**
     * save Data Answer Options
     *
     * @param question             Question
     * @param requestAnswerOptions List<RequestAnswerOption>
     */
    private void saveDataAnswerOptions(Question question, List<RequestAnswerOption> requestAnswerOptions) {
        createAnswerForQuestionYesNo(question, requestAnswerOptions);
        if (ObjectUtils.isEmpty(requestAnswerOptions)) {
            if (Objects.nonNull(question.getId())) {
                answerOptionRepository.deleteAllByQuestionId(question.getId());
            }
            return;
        }
        List<AnswerOption> currentOptions = new ArrayList<>();
        if (Objects.nonNull(question.getId())) {
            currentOptions = answerOptionRepository.getAllByQuestion(question.getId());
        }
        Map<UUID, AnswerOption> currentOptionMap = ObjectUtils.convertToMap(currentOptions, AnswerOption::getId);
        List<AnswerOption> optionsToDelete = new ArrayList<>(currentOptions);
        List<AnswerOption> optionsToSave = new ArrayList<>();
        processAnswerOptions(question, requestAnswerOptions, currentOptionMap, optionsToDelete, optionsToSave);
        answerOptionRepository.saveAll(optionsToSave);
        answerOptionRepository.deleteAll(optionsToDelete);
    }

    public void createAnswerForQuestionYesNo(Question question, List<RequestAnswerOption> requestAnswerOptions) {
        if (!QuestionTypeEnum.RADIOBUTTON.equals(question.getQuestionType()) || !FormConstant.PORTAL_INTAKE_ADDITIONAL_QUESTION.equals(question.getQuestionCode())) {
            return;
        }
        var options = answerOptionRepository.getAllByQuestion(question.getId());
        if (!ObjectUtils.isEmpty(options)) {
            options.forEach(ele -> requestAnswerOptions.add(new RequestAnswerOption(ele.getId(), ele.getAnswer())));
            return;
        }
        requestAnswerOptions.add(new RequestAnswerOption(FormConstant.ANSWER_YES));
        requestAnswerOptions.add(new RequestAnswerOption(FormConstant.ANSWER_NO));
    }


    private void processAnswerOptions(
        Question question,
        List<RequestAnswerOption> requestAnswerOptions,
        Map<UUID, AnswerOption> currentOptionMap,
        List<AnswerOption> optionsToDelete,
        List<AnswerOption> optionsToSave) {
        int priorityOrder = 1;
        for (RequestAnswerOption request : requestAnswerOptions) {
            AnswerOption answerOption = updateOrCreateOption(request, currentOptionMap);
            answerOption.setQuestion(question);
            answerOption.setPriorityOrder(priorityOrder++);
            answerOption.setIsExtra(false);
            if (Objects.nonNull(answerOption.getId())) {
                optionsToDelete.remove(answerOption);
            }
            optionsToSave.add(answerOption);
        }
    }

    /**
     * Get an existing Answer Option by ID or create a new one if it doesn't exist.
     *
     * @param request          RequestAnswerOption object.
     * @param currentOptionMap Map of current questions.
     * @return AnswerOption object.
     */
    private AnswerOption updateOrCreateOption(RequestAnswerOption request, Map<UUID, AnswerOption> currentOptionMap) {
        // Check if the request ID exists and if it matches an existing option in the map
        return Optional.ofNullable(request.getId())
            .map(currentOptionMap::get)
            .map(option -> {
                answerOptionMapper.partialEntity(option, request);
                return option;
            })
            .orElseGet(() -> answerOptionMapper.toEntity(request));
    }

    /**
     * Get an existing question by ID or create a new one if it doesn't exist.
     *
     * @param request            RequestAdditionalQuestion object.
     * @param currentQuestionMap Map of current questions.
     * @return Question object.
     */
    private Question updateOrCreateQuestion(RequestAdditionalQuestion request, Map<UUID, Question> currentQuestionMap, FormCodeEnum formCode) {
        return Optional.ofNullable(request.getId())
            .map(currentQuestionMap::get)
            .map(question -> {
                questionMapper.partialEntityAdditional(question, request);
                return question;
            })
            .orElseGet(() -> questionMapper.toEntityAdditional(
                request,
                formCode,
                FormConstant.PORTAL_INTAKE_ADDITIONAL_QUESTION,
                FormConstant.COLUMN_SIZE_12
            ));
    }


    private void validateAdditionalQuestion(List<RequestAdditionalQuestion> requests, FormCodeEnum formCode) {
        if (ObjectUtils.isEmpty(requests)) {
            return;
        }
        if (FormCodeEnum.PORTAL_INTAKE_ADDITIONAL_QUESTION.equals(formCode) && requests.size() > FormConstant.MAX_PORTAL_ADDITIONAL_QUESTION) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0043, FormConstant.MAX_PORTAL_ADDITIONAL_QUESTION));
        }

        if (FormCodeEnum.TECHNICAL_ASSISTANCE_ADDITIONAL_QUESTION.equals(formCode) && requests.size() > FormConstant.MAX_TECHNICAL_ASSISTANCE_ADDITIONAL_QUESTION) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0043, FormConstant.MAX_TECHNICAL_ASSISTANCE_ADDITIONAL_QUESTION));
        }
        requests.stream().filter(ele -> QuestionTypeEnum.DROPDOWN_MULTIPLE_CHOICE.equals(ele.getQuestionType())
            || QuestionTypeEnum.DROPDOWN_SINGLE_CHOICE.equals(ele.getQuestionType())).forEach(ele -> {
            if (ObjectUtils.isEmpty(ele.getAnswers())) {
                throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0027, "Answer Options"));
            }
        });
    }

    /**
     * Handle extra option
     *
     * @param portalId   UUID
     * @param questionId UUID
     * @param answers    List<ResponseAnswer>
     */
    private void handleExtraOption(UUID portalId, UUID questionId, List<ResponseAnswer> answers) {
        List<AnswerOption> existingAnswerOptions = answerOptionRepository.getAllExtraAnswerByPortalId(portalId, questionId);
        if (ObjectUtils.isEmpty(answers)) {
            answerOptionRepository.deleteAll(existingAnswerOptions);
            return;
        }

        Question question = questionRepository.findById(questionId).orElseThrow(() ->
            new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Question")));

        List<UUID> existingAnswers = existingAnswerOptions.stream()
            .map(AnswerOption::getId)
            .toList();

        // Find new items to add
        List<AnswerOption> addList = answers.stream()
            .filter(answer -> Objects.isNull(answer.getId()))
            .map(answer -> buildAnswerOption(portalId, question, answer.getAnswer(), answer.getPriorityOrder()))
            .collect(Collectors.toList());
        answerOptionRepository.saveAll(addList);

        // Find items to remove
        List<UUID> answerOptionList = answers.stream()
            .map(ResponseAnswer::getId)
            .toList();
        List<UUID> removedItems = new ArrayList<>(existingAnswers);
        removedItems.removeAll(answerOptionList);
        answerOptionRepository.deleteAllById(removedItems);

    }

    private AnswerOption buildAnswerOption(UUID portalId, Question question, String answer, int priorityOrder) {
        return AnswerOption.builder()
            .isExtra(true)
            .answer(answer)
            .priorityOrder(priorityOrder)
            .entryId(portalId)
            .question(question)
            .build();
    }

    public User getCurrentUser() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin)
            .orElse(null);
    }

    private void sendMailSubmitSuccessTechnicalAssistance(String email, String name, Portal portal) {
        String clientAppUrl = inviteService.buildPortalUrl(portal);
        String templatePath = EmailTemplatePathsConstants.SUBMIT_SUCCESS_TECHNICAL_ASSISTANCE_APPLICATION;
        String title = MessageHelper.getMessage("email.business.owner.apply.technical.assistance.title", List.of(KEY_EMPTY));
        String supportEmail = applicationProperties.getCustomerCare().getJoinHuubSupport();
        String supportLiveChat = applicationProperties.getCustomerCare().getJoinHuubLiveChat();
        HashMap<String, Object> mapContents = new HashMap<>();
        mapContents.put(BASE_URL, clientAppUrl);
        mapContents.put(RECEIVER_NAME, name);
        mapContents.put(SUPPORT_EMAIL, supportEmail);
        mapContents.put(SUPPORT_LIVE_CHAT, supportLiveChat);
        mapContents.put(LOGO_IMAGE, fileHelper.primaryPortalLogo(portal.getPrimaryLogo()));
        mailService.sendEmailFromTemplate(email, AppConstants.DEFAULT_LANGUAGE, mapContents, templatePath, title);
    }

    private void sendMailNeedReviewTechnicalAssistance(String email, String name, Portal portal, UUID userId) {
        String clientAppUrl = inviteService.buildPortalUrl(portal);
        String templatePath = EmailTemplatePathsConstants.NEED_REVIEW_SUBMIT_TECHNICAL_ASSISTANCE_APPLICATION;
        String title = MessageHelper.getMessage("email.program.manager.review.technical.assistance.title", List.of(KEY_EMPTY));
        String supportEmail = applicationProperties.getCustomerCare().getJoinHuubSupport();
        String supportLiveChat = applicationProperties.getCustomerCare().getJoinHuubLiveChat();
        HashMap<String, Object> mapContents = new HashMap<>();
        mapContents.put(BASE_URL, clientAppUrl);
        mapContents.put(RECEIVER_NAME, name);
        mapContents.put(SUPPORT_EMAIL, supportEmail);
        mapContents.put(SUPPORT_LIVE_CHAT, supportLiveChat);
        mapContents.put(PORTAL_NAME, portal.getPlatformName());
        mapContents.put(LOGO_IMAGE, fileHelper.primaryPortalLogo(portal.getPrimaryLogo()));
        if (Objects.nonNull(userId)){
            mapContents.put(USER_ID, userId.toString());
        }
        mailService.sendEmailFromTemplate(email, AppConstants.DEFAULT_LANGUAGE, mapContents, templatePath, title);
    }

}
