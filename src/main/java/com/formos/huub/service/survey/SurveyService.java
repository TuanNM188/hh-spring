package com.formos.huub.service.survey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formos.huub.domain.constant.FormConstant;
import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.RoleEnum;
import com.formos.huub.domain.request.survey.*;
import com.formos.huub.domain.response.survey.ResponseSurvey;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.enums.DateTimeFormat;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.exception.SurveyProcessingException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.*;
import com.formos.huub.mapper.survey.SurveyMapper;
import com.formos.huub.repository.PortalRepository;
import com.formos.huub.repository.SurveyRepository;
import com.formos.huub.service.useranswerform.UserFormService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SurveyService {

    SurveyRepository surveyRepository;

    SurveyMapper surveyMapper;

    PortalRepository portalRepository;

    UserFormService userFormService;

    private static final String COPY_OF = "Copy of ";
    private static final String TITLE_OF_SURVEY_JSON = "title";
    private static final String[] TITLE_OF_SURVEY_RESPONSES_CSV = new String[] {
        "Date",
        "First Name",
        "Last Name",
        "Member Email",
        "Role",
        "Business Name",
    };
    private static final String DELIMITER_CSV = ",";

    private static final String KEY_PAGES = "pages";
    private static final String KEY_ELEMENTS = "elements";
    private static final String KEY_NAME = "name";
    private static final String KEY_TYPE = "type";
    private static final String KEY_ANSWER = "answer";
    private static final String KEY_NODE = "node";
    private static final String KEY_INPUTTYPE = "inputType";
    private static final String KEY_TITLE = "title";
    private static final String KEY_CHOICES = "choices";
    private static final String KEY_TEXT = "text";
    private static final String KEY_VALUE = "value";
    private static final String KEY_RATEVALUES = "rateValues";
    private static final String NA = "N/A";
    private static final String YES = "Yes";
    private static final String NO = "No";
    private static final String NONE = "None";

    private static final String QUESTION_TYPE_BOOLEAN = "boolean";
    private static final String QUESTION_TYPE_FILE = "file";
    private static final String QUESTION_TYPE_MULTIPLETEXT = "multipletext";
    private static final String QUESTION_TYPE_MATRIX = "matrix";
    private static final String QUESTION_TYPE_MATRIXDROPDOWN = "matrixdropdown";
    private static final String QUESTION_TYPE_MATRIXDYNAMIC = "matrixdynamic";
    private static final String QUESTION_TYPE_PANELDYNAMIC = "paneldynamic";
    private static final String QUESTION_TYPE_PANEL = "panel";

    private static final String QUESTION_TYPE_TEXT = "text";
    private static final String QUESTION_TYPE_TEXT_DATETIME_LOCAL = "datetime-local";
    private static final String QUESTION_TYPE_TEXT_DATE = "date";

    private static final String QUESTION_TYPE_RADIOGROUP = "radiogroup";
    private static final String QUESTION_TYPE_CHECKBOX = "checkbox";
    private static final String QUESTION_TYPE_DROPDOWN = "dropdown";
    private static final String QUESTION_TYPE_TAGBOX = "tagbox";
    private static final String QUESTION_TYPE_RANKING = "ranking";

    private static final String QUESTION_TYPE_RATING = "rating";

    /**
     * search Survey
     *
     * @param request RequestSearchSurvey
     * @return Map<String, Object>
     */
    public Map<String, Object> searchSurvey(RequestSearchSurvey request) {
        var portalContext = PortalContextHolder.getContext();
        if (Objects.nonNull(portalContext)) {
            request.setPortalId(portalContext.getPortalId());
        }
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "createdDate,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        var data = surveyRepository.searchSurveyByTermAndCondition(request, pageable);
        return PageUtils.toPage(data);
    }

    /**
     * check Exist Survey Name
     *
     * @param surveyName String
     * @param surveyId   UUID
     * @return boolean
     */
    public boolean checkExistSurveyName(String surveyName, UUID surveyId) {
        var isExistSurveyName = Boolean.FALSE;
        if (Objects.nonNull(surveyId)) {
            isExistSurveyName = surveyRepository.existsByNameIgnoreCaseAndIdNot(surveyName, surveyId);
        } else {
            isExistSurveyName = surveyRepository.existsByNameIgnoreCase(surveyName);
        }
        return isExistSurveyName;
    }

    /**
     * get Portal by ID
     *
     * @param id UUID
     * @return Portal Entity
     */
    private Portal getPortal(UUID id) {
        return portalRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Portal")));
    }

    /**
     * create Survey
     *
     * @param request RequestCreateSurvey
     */
    public UUID createSurvey(@Valid RequestCreateSurvey request) {
        var isExistSurveyName = surveyRepository.existsByNameIgnoreCase(request.getName());
        if (isExistSurveyName) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "survey"));
        }
        var portalId = UUIDUtils.toUUID(request.getPortalId());
        if (Objects.isNull(portalId)) {
            portalId = PortalContextHolder.getPortalId();
        }
        var survey = new Survey();
        survey = surveyMapper.toEntity(request, portalId);
        survey.setIsActive(Optional.ofNullable(survey.getIsActive()).orElse(Boolean.TRUE));
        survey.setSurveyJson(Optional.ofNullable(survey.getSurveyJson()).orElse(new JsonObject().toString()));
        var response = surveyRepository.save(survey);
        return response.getId();
    }

    /**
     * get Survey entity
     *
     * @param surveyId UUID
     */
    private Survey getSurvey(UUID surveyId) {
        return surveyRepository
            .findById(surveyId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Survey")));
    }

    /**
     * get Survey
     *
     * @param surveyId UUID
     */
    public ResponseSurvey getDetail(UUID surveyId) {
        var survey = getSurvey(surveyId);
        return surveyMapper.toResponse(survey);
    }

    /**
     * update Survey
     *
     * @param surveyId UUID
     * @param request  RequestUpdateSurvey
     */
    public Object updateSurvey(UUID surveyId, @Valid RequestUpdateSurvey request) {
        var survey = getSurvey(surveyId);
        var isExistSurveyName = surveyRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), surveyId);
        if (isExistSurveyName) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "survey"));
        }
        survey = surveyMapper.partialUpdate(survey, request);
        var portalId = UUIDUtils.toUUID(request.getPortalId());
        if (!Objects.isNull(portalId) && !portalId.equals(survey.getPortal().getId())) {
            survey.setPortal(getPortal(portalId));
        }
        survey.setSurveyJson(Optional.ofNullable(survey.getSurveyJson()).orElse(new JsonObject().toString()));
        var response = surveyRepository.save(survey);
        return surveyMapper.toResponse(response);
    }

    /**
     * Generate the count of duplicate survey names.
     *
     * @param originalName String
     */
    private String generateUniqueDuplicateName(String originalName) {
        String baseName = COPY_OF + originalName;
        String currentName = baseName;
        int counter = 1;
        while (surveyRepository.existsByNameIgnoreCase(currentName)) {
            currentName = baseName + " (" + counter++ + ")";
        }
        return currentName;
    }

    /**
     * duplicate Survey
     *
     * @param request RequestDuplicateSurvey
     */
    public UUID duplicateSurvey(@Valid RequestDuplicateSurvey request) {
        var sourceSurvey = getSurvey(UUID.fromString(request.getId()));
        String duplicatedName = generateUniqueDuplicateName(sourceSurvey.getName());
        try {
            JsonObject surveyJson = JsonParser.parseString(
                Optional.ofNullable(sourceSurvey.getSurveyJson()).orElse(new JsonObject().toString())
            ).getAsJsonObject();
            surveyJson.addProperty(TITLE_OF_SURVEY_JSON, duplicatedName);
            var requestCreateSurvey = RequestCreateSurvey.builder()
                .name(duplicatedName)
                .description(sourceSurvey.getDescription())
                .portalId(sourceSurvey.getPortal().getId().toString())
                .surveyJson(surveyJson.toString())
                .build();
            var newSurvey = new Survey();
            newSurvey = surveyMapper.toEntity(requestCreateSurvey, sourceSurvey.getPortal().getId());
            newSurvey.setIsActive(Optional.ofNullable(newSurvey.getIsActive()).orElse(Boolean.TRUE));
            var response = surveyRepository.save(newSurvey);
            return response.getId();
        } catch (Exception e) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "surveyJson"));
        }
    }

    /**
     * update status Survey
     *
     * @param request RequestUpdateStatusSurvey
     */
    public Object updateStatus(@Valid RequestUpdateStatusSurvey request) {
        var survey = getSurvey(UUID.fromString(request.getId()));
        survey.setIsActive(request.getIsActive());
        var response = surveyRepository.save(survey);
        return surveyMapper.toResponse(response);
    }

    /**
     * export Survey Responses csv file
     *
     * @param surveyId UUID
     */
    public ByteArrayOutputStream downloadSurveyResponses(UUID surveyId) {
        try {
            Survey survey = getSurvey(surveyId);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintWriter writer = initializeCsvWriter(out);

            writeCsvHeader(writer, survey);
            survey
                .getResponses()
                .forEach(responses -> {
                    try {
                        processSurveyResponse(survey, responses, writer);
                    } catch (JsonProcessingException e) {
                        // Log error but continue processing other responses
                        log.error("Error processing survey response: {}", responses.getId(), e);
                    }
                });
            writer.flush();
            return out;
        } catch (JsonProcessingException e) {
            throw new SurveyProcessingException("Error getting survey data", e);
        } catch (Exception e) {
            throw new SurveyProcessingException("Unexpected error during survey download", e);
        }
    }

    /**
     * initialize Csv Writer
     *
     * @param out ByteArrayOutputStream
     */
    private PrintWriter initializeCsvWriter(ByteArrayOutputStream out) {
        return new PrintWriter(out, true, StandardCharsets.UTF_8);
    }

    /**
     * write Csv Header
     *
     * @param writer PrintWriter
     * @param survey Survey
     */
    private void writeCsvHeader(PrintWriter writer, Survey survey) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(survey.getSurveyJson());
        List<Map<String, JsonNode>> questionAndTypeList = extractQuestions(rootNode);
        List<String> hardHeaders = Arrays.asList(TITLE_OF_SURVEY_RESPONSES_CSV);
        List<String> questionList = questionAndTypeList.stream().map(entry -> entry.get(KEY_TITLE).asText()).toList();
        List<String> headers = Stream.concat(hardHeaders.stream(), questionList.stream())
            .map(this::wrapValueForCsv)
            .collect(Collectors.toList());
        writer.println(String.join(DELIMITER_CSV, headers));
    }

    /**
     * process Survey Response
     *
     * @param survey          Survey
     * @param surveyResponses SurveyResponses
     * @param writer          PrintWriter
     */
    private void processSurveyResponse(Survey survey, SurveyResponses surveyResponses, PrintWriter writer) throws JsonProcessingException {
        User user = surveyResponses.getUser();
        String originalRole = getUserOriginalRole(user);
        String businessName = getBusinessNameIfApplicable(user, originalRole);
        List<String> surveyResponseLine = buildSurveyResponseLine(survey, surveyResponses, user, originalRole, businessName);
        writer.println(String.join(DELIMITER_CSV, surveyResponseLine));
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
     * get Business Name If Applicable
     *
     * @param user         User
     * @param originalRole String
     */
    private String getBusinessNameIfApplicable(User user, String originalRole) {
        if (RoleEnum.ROLE_BUSINESS_OWNER.getValue().equals(originalRole)) {
            var answerBusinessInfoMap = userFormService.getAnswerUserByQuestionCode(
                user.getId(),
                List.of(FormConstant.PORTAL_INTAKE_QUESTION_BUSINESS)
            );
            return Optional.ofNullable(answerBusinessInfoMap.get(FormConstant.PORTAL_INTAKE_QUESTION_BUSINESS)).orElse(StringUtils.EMPTY);
        }
        return StringUtils.EMPTY;
    }

    /**
     * build Survey Response Line
     *
     * @param survey          Survey
     * @param businessName    String
     * @param originalRole    String
     * @param surveyResponses SurveyResponses
     * @param user            User
     */
    private List<String> buildSurveyResponseLine(
        Survey survey,
        SurveyResponses surveyResponses,
        User user,
        String originalRole,
        String businessName
    ) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNodeSurveyJson = objectMapper.readTree(survey.getSurveyJson());
        JsonNode rootNodeSurveyData = objectMapper.readTree(surveyResponses.getSurveyData());
        List<Map<String, JsonNode>> questionAndTypeList = extractQuestions(rootNodeSurveyJson);
        List<Map<String, String>> questionAndAnswerList = extractAnswers(rootNodeSurveyData, questionAndTypeList);
        List<String> hardValueOfLine = Arrays.asList(
            DateUtils.convertInstantToStringTime(surveyResponses.getSubmissionDate(), DateTimeFormat.MM_DD_YYYY),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            RoleEnum.fromRoleCode(originalRole).getName(),
            businessName
        );
        List<String> answerList = questionAndAnswerList.stream().map(entry -> entry.get(KEY_ANSWER)).toList();
        return Stream.concat(hardValueOfLine.stream(), answerList.stream()).map(this::wrapValueForCsv).collect(Collectors.toList());
    }

    /**
     * Get list of questions from JSON
     */
    private List<Map<String, JsonNode>> extractQuestions(JsonNode rootNode) {
        return StreamSupport.stream(rootNode.path(KEY_PAGES).spliterator(), false)
            .flatMap(page -> StreamSupport.stream(page.path(KEY_ELEMENTS).spliterator(), false))
            .flatMap(this::extractQuestionsFromParentNode)
            .toList();
    }

    /**
     * extract Questions from Parent Node
     */
    private Stream<Map<String, JsonNode>> extractQuestionsFromParentNode(JsonNode node) {
        if (QUESTION_TYPE_PANEL.equals(node.get(KEY_TYPE).asText())) {
            return StreamSupport.stream(node.path(KEY_ELEMENTS).spliterator(), false).flatMap(this::extractQuestionsFromParentNode);
        }
        Map<String, JsonNode> questionData = Map.of(
            KEY_NAME,
            node.get(KEY_NAME),
            KEY_TITLE,
            node.has(KEY_TITLE) ? node.get(KEY_TITLE) : node.get(KEY_NAME),
            KEY_TYPE,
            node.get(KEY_TYPE),
            KEY_NODE,
            node
        );
        return Stream.of(questionData);
    }

    /**
     * Get list of responses from JSON
     */
    private List<Map<String, String>> extractAnswers(JsonNode rootNode, List<Map<String, JsonNode>> questionList) {
        return questionList
            .stream()
            .map(
                question ->
                    Map.of(
                        KEY_ANSWER,
                        parseAnswer(question, rootNode.path(question.get(KEY_NAME).asText())),
                        KEY_NAME,
                        question.get(KEY_NAME).asText(),
                        KEY_TYPE,
                        question.get(KEY_TYPE).asText()
                    )
            )
            .toList();
    }

    /**
     * Convert answer values
     */
    private String parseAnswer(Map<String, JsonNode> question, JsonNode answerNode) {
        if (answerNode == null) return NA;
        return switch (getTypeQuestion(question)) {
            case QUESTION_TYPE_MATRIX,
                QUESTION_TYPE_MATRIXDROPDOWN,
                QUESTION_TYPE_MATRIXDYNAMIC,
                QUESTION_TYPE_MULTIPLETEXT,
                QUESTION_TYPE_PANELDYNAMIC -> formatTextAnswer(answerNode.toString());
            case QUESTION_TYPE_CHECKBOX, QUESTION_TYPE_TAGBOX, QUESTION_TYPE_RANKING -> parseMultipleChoice(answerNode, question);
            case QUESTION_TYPE_RADIOGROUP, QUESTION_TYPE_DROPDOWN -> parseSingleChoice(answerNode, question, KEY_CHOICES);
            case QUESTION_TYPE_FILE -> answerNode.isArray() && !answerNode.isEmpty() ? answerNode.get(0).path(KEY_NAME).asText() : NA;
            case QUESTION_TYPE_RATING -> parseSingleChoice(answerNode, question, KEY_RATEVALUES);
            case QUESTION_TYPE_TEXT_DATETIME_LOCAL, QUESTION_TYPE_TEXT_DATE -> formatDateAnswer(answerNode, getTypeQuestion(question));
            case QUESTION_TYPE_BOOLEAN -> answerNode.asBoolean() ? YES : NO;
            default -> formatTextAnswer(answerNode.asText());
        };
    }

    private String formatTextAnswer(String answer) {
        if (NONE.equalsIgnoreCase(answer)) {
            return NONE;
        }
        return answer;
    }

    private String formatDateAnswer(JsonNode answerNode, String questionType) {
        if (Optional.ofNullable(answerNode.asText()).isEmpty() || answerNode.asText().isEmpty()) {
            return NA;
        }
        String format = QUESTION_TYPE_TEXT_DATE.equals(questionType)
            ? DateTimeFormat.MM_DD_YYYY.getValue()
            : DateTimeFormat.MM_dd_yyyy_HH_mm.getValue();
        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern(format);
        return answerNode.asText().length() == 10
            ? LocalDate.parse(answerNode.asText(), DateTimeFormatter.ISO_LOCAL_DATE).format(outputFormat)
            : LocalDateTime.parse(answerNode.asText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME).format(outputFormat);
    }

    private String getTypeQuestion(Map<String, JsonNode> question) {
        String questionType = question.get(KEY_TYPE).asText();
        if (QUESTION_TYPE_TEXT.equals(questionType) && question.get(KEY_NODE).has(KEY_INPUTTYPE)) {
            return question.get(KEY_NODE).get(KEY_INPUTTYPE).asText();
        }
        return questionType;
    }

    private String parseSingleChoice(JsonNode answerNode, Map<String, JsonNode> question, String key) {
        String titleOfAnswer = answerNode.asText();
        for (JsonNode choice : question.get(KEY_NODE).path(key)) {
            if (choice.has(KEY_VALUE) && choice.has(KEY_TEXT) && answerNode.asText().equals(choice.get(KEY_VALUE).asText())) {
                return formatTextAnswer(choice.get(KEY_TEXT).asText());
            }
        }
        return formatTextAnswer(titleOfAnswer);
    }

    private String parseMultipleChoice(JsonNode answerNode, Map<String, JsonNode> question) {
        List<String> selectedValues = new ArrayList<>();
        if (answerNode.isArray()) {
            for (JsonNode item : answerNode) {
                selectedValues.add(parseSingleChoice(item, question, KEY_CHOICES));
            }
        }
        return String.join(DELIMITER_CSV, selectedValues);
    }

    /**
     * wrap Value For Csv
     */
    private String wrapValueForCsv(String value) {
        if (value != null && (value.contains(DELIMITER_CSV) || value.contains("\"") || value.contains("\n"))) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
