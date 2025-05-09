package com.formos.huub.service.metricsreport;

import static com.formos.huub.domain.constant.FormConstant.PORTAL_INTAKE_QUESTION_BUSINESS;

import com.formos.huub.domain.constant.BusinessConstant;
import com.formos.huub.domain.constant.FormConstant;
import com.formos.huub.domain.constant.MetricExportConstant;
import com.formos.huub.domain.entity.Portal;
import com.formos.huub.domain.entity.Question;
import com.formos.huub.domain.entity.UserAnswerForm;
import com.formos.huub.domain.enums.EntryTypeEnum;
import com.formos.huub.domain.enums.FormCodeEnum;
import com.formos.huub.domain.enums.QuestionTypeEnum;
import com.formos.huub.domain.enums.ReportExportTypeEnum;
import com.formos.huub.domain.request.metricsreport.RequestSearchAppointmentProjectReport;
import com.formos.huub.domain.response.answerform.ResponseUserAnswerForm;
import com.formos.huub.domain.response.calendarevent.IResponseEventRegistrationExport;
import com.formos.huub.domain.response.communitypartner.IResponseAssignAdvisor;
import com.formos.huub.domain.response.invoice.IResponseInvoiceExport;
import com.formos.huub.domain.response.learninglibrary.IResponseCourseExport;
import com.formos.huub.domain.response.project.IResponseProjectExport;
import com.formos.huub.domain.response.report.ResponseOverviewApplicationReport;
import com.formos.huub.domain.response.technicalassistance.IResponseAppointmentExport;
import com.formos.huub.domain.response.technicalassistance.IResponseInfoApplication;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.enums.DateTimeFormat;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.properties.ScheduleProperties;
import com.formos.huub.framework.utils.*;
import com.formos.huub.repository.*;
import com.formos.huub.service.applicationmanagement.ApplicationManagementService;
import com.formos.huub.service.portals.PortalFormService;
import com.formos.huub.service.useranswerform.UserFormService;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MetricsReportService {

    UserFormService userFormService;

    BusinessOwnerRepository businessOwnerRepository;

    QuestionRepository questionRepository;

    PortalRepository portalRepository;

    TechnicalAssistanceSubmitRepository technicalAssistanceSubmitRepository;

    AppointmentRepository appointmentRepository;

    UserAnswerFormRepository userAnswerFormRepository;

    ProjectRepository projectRepository;

    LearningLibraryRepository learningLibraryRepository;

    EventRegistrationRepository eventRegistrationRepository;

    ScheduleProperties scheduleProperties;

    MetricsReportRepositoryCustom metricsReportRepositoryCustom;

    AppointmentReportRepository appointmentReportRepository;

    MetabaseTokenService metabaseTokenService;

    ApplicationManagementService applicationManagementService;

    InvoiceRepository invoiceRepository;

    PortalFormService portalFormService;

    public Map<String, Object> searchInvoiceAmountByAdvisor(RequestSearchAppointmentProjectReport request) {
        if (Objects.isNull(request.getTimezone())) {
            request.setTimezone(scheduleProperties.getTimezone());
        }
        request.setPortalIds(applicationManagementService.getListPortalByRole(request.getPortalId()));
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "invoiceDate,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        HashMap<String, String> sortMap = new HashMap<>();
        sortMap.put(BusinessConstant.TIMEZONE_KEY, request.getTimezone());
        request.setSearchConditions(ObjectUtils.convertSortParams(request.getSearchConditions(), sortMap));
        return PageUtils.toPage(metricsReportRepositoryCustom.getAllInvoiceAmountByAdvisorPageable(request, pageable));
    }

    public Map<String, Object> searchAppointmentProjectReports(RequestSearchAppointmentProjectReport request) {
        if (Objects.isNull(request.getTimezone())) {
            request.setTimezone(scheduleProperties.getTimezone());
        }
        request.setPortalIds(applicationManagementService.getListPortalByRole(request.getPortalId()));
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "reportSubmissionDate,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        return PageUtils.toPage(metricsReportRepositoryCustom.searchAppointmentProjectReports(request, pageable));
    }

    public ResponseOverviewApplicationReport getOverviewApplicationData(UUID portalId, String startDate, String endDate, String timezone) {
        if (Objects.isNull(timezone)) {
            timezone = scheduleProperties.getTimezone();
        }
        var portalIds = applicationManagementService.getListPortalByRole(portalId);
        var dataRating = appointmentReportRepository.numAverageRatingAdvisor(portalIds, startDate, endDate, timezone);
        var response = new ResponseOverviewApplicationReport();
        BeanUtils.copyProperties(dataRating, response);
        var monthlyExpense = invoiceRepository.getNumMonthlyExpense(portalIds, startDate, endDate, timezone);
        response.setMonthlyExpense(monthlyExpense.getMonthlyExpense());
//        response.setTotalHours(monthlyExpense.getTotalHours());

        if (Objects.nonNull(portalId)) {
            var currentTerm = portalFormService.getCurrentTerm(portalId);
            if (Objects.nonNull(currentTerm)) {
                response.setStartDate(currentTerm.getStartDate());
                response.setEndDate(currentTerm.getEndDate());
            }
        }
        return response;
    }

    /**
     * generate Token Metabase For Portal Host
     *
     * @param dashboard Integer
     * @return String token
     */
    public String generateTokenMetabaseForPortalHost(Integer dashboard, String portalId, String timezone) {
        String portalName = null;
        if (Objects.isNull(portalId)) {
            portalName = Objects.requireNonNull(PortalContextHolder.getContext().getPlatformName());
        }
        if (Objects.isNull(timezone)) {
            timezone = scheduleProperties.getTimezone();
        }
        return metabaseTokenService.getIframeUrl(dashboard, portalName, timezone);
    }

    /**
     * Exports a report based on the specified type.
     *
     * @param response         HttpServletResponse
     * @param reportExportType ReportExportTypeEnum
     * @param portalId         UUID
     * @param startDate        String
     * @param endDate          String
     * @param timezone         String
     */
    public void exportReportByType(
        HttpServletResponse response,
        ReportExportTypeEnum reportExportType,
        UUID portalId,
        String startDate,
        String endDate,
        String timezone
    ) {
        portalId = Objects.requireNonNullElse(portalId, PortalContextHolder.getPortalId());
        var portal = portalRepository
            .findById(portalId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Portal")));
        timezone = Objects.requireNonNullElse(timezone, scheduleProperties.getTimezone());
        switch (reportExportType) {
            case BUSINESS_OWNER_INTAKES -> exportBusinessOwnerData(response, portal, startDate, endDate, timezone);
            case TECHNICAL_ASSISTANCE_APPLICATIONS -> exportTechnicalAssistanceData(response, portal, startDate, endDate, timezone);
            case APPOINTMENTS -> exportAppointmentsData(response, portal, startDate, endDate, timezone);
            case PROJECTS -> exportProjectsData(response, portal, startDate, endDate, timezone);
            case EVENT_REGISTRATIONS -> exportEventRegistrationData(response, portal, startDate, endDate, timezone);
            case COURSES -> exportCoursesData(response, portal, startDate, endDate, timezone);
            case TECHNICAL_ASSISTANCE_INVOICE -> exportInvoiceData(response, portal, startDate, endDate, timezone);
            default -> log.warn("Export type {} is not implemented", reportExportType);
        }
    }

    /**
     * Exports business owner intake data to a CSV file.
     *
     * @param response  HttpServletResponse
     * @param portal    Portal
     * @param startDate String
     * @param endDate   String
     * @param timezone  String
     */
    private void exportBusinessOwnerData(HttpServletResponse response, Portal portal, String startDate, String endDate, String timezone) {
        exportData(
            response,
            portal,
            startDate,
            endDate,
            ReportExportTypeEnum.BUSINESS_OWNER_INTAKES.getName(),
            () -> businessOwnerRepository.getBusinessOwnerByPortalIdAndDateBetween(portal.getId(), startDate, endDate, timezone),
            getBusinessOwnerHeaders(portal.getId()),
            data -> mapBusinessOwnerData(data, portal, timezone)
        );
    }

    /**
     * Exports technical assistance application data to a CSV file.
     *
     * @param response  HttpServletResponse
     * @param portal    Portal
     * @param startDate String
     * @param endDate   String
     * @param timezone  String
     */
    private void exportTechnicalAssistanceData(
        HttpServletResponse response,
        Portal portal,
        String startDate,
        String endDate,
        String timezone
    ) {
        exportData(
            response,
            portal,
            startDate,
            endDate,
            ReportExportTypeEnum.TECHNICAL_ASSISTANCE_APPLICATIONS.getName(),
            () -> technicalAssistanceSubmitRepository.getAllApplicationByPortal(portal.getId(), startDate, endDate, timezone),
            getTechnicalAssistanceApplicationHeaders(portal.getId()),
            data -> mapTechnicalAssistanceData(data, portal, timezone)
        );
    }

    /**
     * Exports appointment data to a CSV file.
     *
     * @param response  HttpServletResponse
     * @param portal    Portal
     * @param startDate String
     * @param endDate   String
     */
    private void exportAppointmentsData(HttpServletResponse response, Portal portal, String startDate, String endDate, String timezone) {
        exportData(
            response,
            portal,
            startDate,
            endDate,
            ReportExportTypeEnum.APPOINTMENTS.getName(),
            () -> appointmentRepository.getAllByPortalAndDate(portal.getId(), startDate, endDate, timezone),
            MetricExportConstant.LIST_HEADER_APPOINTMENTS_EXPORT,
            this::mapAppointmentsData
        );
    }

    /**
     * Exports project data to a CSV file.
     *
     * @param response  HttpServletResponse
     * @param portal    Portal
     * @param startDate String
     * @param endDate   String
     * @param timezone  String
     */
    private void exportProjectsData(HttpServletResponse response, Portal portal, String startDate, String endDate, String timezone) {
        exportData(
            response,
            portal,
            startDate,
            endDate,
            ReportExportTypeEnum.PROJECTS.getName(),
            () -> projectRepository.getAllByPortalAndDate(portal.getId(), startDate, endDate, timezone),
            MetricExportConstant.LIST_HEADER_PROJECTS_EXPORT,
            this::mapProjectsToData
        );
    }

    /**
     * Exports course data to a CSV file.
     *
     * @param response  HttpServletResponse
     * @param portal    Portal
     * @param startDate String
     * @param endDate   String
     * @param timezone  String
     */
    private void exportEventRegistrationData(
        HttpServletResponse response,
        Portal portal,
        String startDate,
        String endDate,
        String timezone
    ) {
        exportData(
            response,
            portal,
            startDate,
            endDate,
            ReportExportTypeEnum.EVENT_REGISTRATIONS.getName(),
            () -> eventRegistrationRepository.getAllByPortalAndDate(portal.getId(), startDate, endDate, timezone),
            MetricExportConstant.LIST_HEADER_EVENT_REGISTRATION_EXPORT,
            this::mapEventsToData
        );
    }

    /**
     * Exports course data to a CSV file.
     *
     * @param response  HttpServletResponse
     * @param portal    Portal
     * @param startDate String
     * @param endDate   String
     * @param timezone  String
     */
    private void exportCoursesData(HttpServletResponse response, Portal portal, String startDate, String endDate, String timezone) {
        exportData(
            response,
            portal,
            startDate,
            endDate,
            ReportExportTypeEnum.COURSES.getName(),
            () -> learningLibraryRepository.getAllByPortalAndDate(portal.getId(), startDate, endDate, timezone),
            MetricExportConstant.LIST_HEADER_COURSES_EXPORT,
            this::mapCourseToData
        );
    }

    /**
     * Exports invoice data to a CSV file.
     *
     * @param response  HttpServletResponse
     * @param portal    Portal
     * @param startDate String
     * @param endDate   String
     * @param timezone  String
     */
    private void exportInvoiceData(HttpServletResponse response, Portal portal, String startDate, String endDate, String timezone) {
        exportData(
            response,
            portal,
            startDate,
            endDate,
            ReportExportTypeEnum.COURSES.getName(),
            () -> invoiceRepository.getAllByPortalAndDate(portal.getId(), startDate, endDate, timezone),
            MetricExportConstant.LIST_HEADER_INVOICES_EXPORT,
            this::mapInvoiceToData
        );
    }

    /**
     * Generic method to export data to a CSV file.
     * Fetches data using the provided supplier, maps it, and writes it to the response.
     *
     * @param response     HttpServletResponse
     * @param portal       Portal
     * @param startDate    String
     * @param endDate      String
     * @param reportName   String
     * @param dataSupplier Supplier<List<T>>
     * @param headers      List<String>
     * @param mapper       Function<List<T>, List<Map<String, String>>>
     * @param <T>          Data type
     */
    private <T> void exportData(
        HttpServletResponse response,
        Portal portal,
        String startDate,
        String endDate,
        String reportName,
        Supplier<List<T>> dataSupplier,
        List<String> headers,
        Function<List<T>, List<Map<String, String>>> mapper
    ) {
        UUID portalId = portal.getId();
        try {
            List<T> dataList = dataSupplier.get();
            if (dataList.isEmpty()) {
                log.warn("No {} data found for export in portal {} between {} and {}", reportName, portalId, startDate, endDate);
            }
            List<Map<String, String>> mappedData = mapper.apply(dataList);
            FileUtils.exportCsv(response, headers, mappedData, reportName);
        } catch (Exception e) {
            log.error("Export failed for {}: {}", reportName, e.getMessage(), e);
        }
    }

    private List<String> getQuestionsFromRepository(String questionCodes) {
        return getQuestionsFromRepository(repo -> repo.getAllByQuestionCodeInOrderBy(questionCodes));
    }

    private List<String> getQuestionsFromRepository(UUID portalId, FormCodeEnum formCode) {
        return getQuestionsFromRepository(repo -> repo.getAllByPortalAndFormCode(portalId, formCode));
    }

    private List<String> getQuestionsFromRepository(Function<QuestionRepository, List<Question>> queryFunction) {
        var questions = Optional.ofNullable(queryFunction.apply(questionRepository)).orElse(Collections.emptyList());
        var questionMap = ObjectUtils.convertToMap(questions, Question::getId);
        return questions
            .stream()
            .map(question -> {
                String text = getFullQuestionText(question, questionMap);
                return appendAnswer(question.getQuestionCode(), text);
            })
            .toList();
    }

    private String getFullQuestionText(Question question, Map<UUID, Question> questionMap) {
        UUID parentId = question.getParentId();
        if (parentId != null) {
            Question parent = questionMap.get(parentId);
            if (parent != null) {
                return parent.getQuestion().replace("?", " Other input");
            }
        }
        return question.getQuestion();
    }

    private String appendAnswer(String code, String value) {
        return FormConstant.LIST_QUESTION_DUPLICATE_HEADER.contains(code)
            ? value.concat(" ")
            : value;
    }

    /**
     * Extracted method: Map appointments to a structured data format
     *
     * @param appointments List<IResponseAppointmentExport>
     * @return List<Map < String, String>>
     */
    private List<Map<String, String>> mapAppointmentsData(List<IResponseAppointmentExport> appointments) {
        if (appointments.isEmpty()) {
            appointments = List.of();
        }
        return appointments.stream().map(this::mapAppointmentFields).collect(Collectors.toList());
    }

    /**
     * Maps appointment-specific fields.
     */
    private Map<String, String> mapAppointmentFields(IResponseAppointmentExport appointment) {
        Map<String, String> response = new HashMap<>();

        // Add appointment-specific details
        response.put(MetricExportConstant.COLUMN_ADVISOR_NAME, appointment.getAdvisorName());
        response.put(MetricExportConstant.COLUMN_VENDOR, appointment.getVendor());
        response.put(MetricExportConstant.COLUMN_APPOINTMENT_CATEGORY, appointment.getAppointmentCategory());
        response.put(MetricExportConstant.COLUMN_APPOINTMENT_SERVICE, appointment.getAppointmentService());
        response.put(MetricExportConstant.COLUMN_STATUS, appointment.getStatus());
        response.put(MetricExportConstant.COLUMN_START_DATE_TIME, appointment.getStartDate());
        response.put(MetricExportConstant.COLUMN_END_DATE_TIME, appointment.getEndDate());
        response.put(MetricExportConstant.COLUMN_DESCRIPTION_LIKE_SUPPORT_FROM_ADVISOR, appointment.getSupportDescription());
        response.put(MetricExportConstant.COLUMN_URL_SHARE_YOUR_ADVISOR, appointment.getShareLinks());
        response.put(MetricExportConstant.COLUMN_APPOINTMENT_SERVICE_OUTCOME, appointment.getServiceOutcomes());
        response.put(MetricExportConstant.COLUMN_RELATED_INVOICE, appointment.getInvoiceNumber());
        response.put(MetricExportConstant.COLUMN_REPORT, appointment.getReportNumber());
        addCommonDetails(response, appointment.getCreatedDate(), appointment.getPortalName());
        addPersonalDetails(
            response,
            appointment.getFirstName(),
            appointment.getLastName(),
            appointment.getEmail(),
            StringUtils.EMPTY,
            appointment.getBusinessName()
        );
        return response;
    }

    /**
     * Extracted method: Map projects to a structured data format
     *
     * @param projects List<IResponseProjectExport>
     * @return List<Map < String, String>>
     */
    private List<Map<String, String>> mapProjectsToData(List<IResponseProjectExport> projects) {
        return projects.stream().map(this::mapProjectFields).collect(Collectors.toList());
    }

    /**
     * Maps project-specific fields.
     */
    private Map<String, String> mapProjectFields(IResponseProjectExport project) {
        Map<String, String> response = new HashMap<>();

        // Add project-specific details
        response.put(MetricExportConstant.COLUMN_ADVISOR_NAME, project.getAdvisorName());
        response.put(MetricExportConstant.COLUMN_VENDOR, project.getVendor());
        response.put(MetricExportConstant.COLUMN_PROJECT_TITLE, project.getProjectName());
        response.put(MetricExportConstant.COLUMN_STATUS, project.getStatus());
        response.put(MetricExportConstant.COLUMN_SCOPE_OF_WORK, project.getScopeOfWork());
        response.put(MetricExportConstant.COLUMN_ESTIMATED_HOURS, String.valueOf(project.getEstimatedHoursNeeded()));
        response.put(MetricExportConstant.COLUMN_PROPOSED_START_DATE, project.getProposedStartDate());
        response.put(MetricExportConstant.COLUMN_ESTIMATED_COMPLETION_DATE, project.getEstimatedCompletionDate());
        response.put(MetricExportConstant.COLUMN_RELATED_APPOINTMENT, project.getRelatedAppointmentDate());
        response.put(MetricExportConstant.COLUMN_SERVICE, project.getServiceName());
        response.put(MetricExportConstant.COLUMN_CATEGORY, project.getCategoryName());
        response.put(MetricExportConstant.COLUMN_RELATED_INVOICE, project.getInvoiceNumber());
        response.put(MetricExportConstant.COLUMN_REPORT, project.getReportNumber());

        addCommonDetails(response, project.getCreatedDate(), project.getPortalName());
        addPersonalDetails(
            response,
            project.getFirstName(),
            project.getLastName(),
            project.getEmail(),
            StringUtils.EMPTY,
            project.getBusinessName()
        );
        return response;
    }

    /**
     * Extracted method: Map courses to a structured data format
     *
     * @param courseExports List<IResponseCourseExport>
     * @return List<Map < String, String>>
     */
    private List<Map<String, String>> mapCourseToData(List<IResponseCourseExport> courseExports) {
        return courseExports.stream().map(this::mapCourseFields).collect(Collectors.toList());
    }

    /**
     * Extracted method: Map courses to a structured data format
     *
     * @param invoiceExports List<IResponseInvoiceExport>
     * @return List<Map < String, String>>
     */
    private List<Map<String, String>> mapInvoiceToData(List<IResponseInvoiceExport> invoiceExports) {
        return invoiceExports.stream().map(this::mapInvoiceFields).collect(Collectors.toList());
    }

    /**
     * Maps course-specific fields.
     */
    private Map<String, String> mapCourseFields(IResponseCourseExport course) {
        Map<String, String> response = new HashMap<>();

        // Add course-specific details
        response.put(MetricExportConstant.COLUMN_COURSE_NAME, course.getCourseName());
        response.put(MetricExportConstant.COLUMN_COURSE_CATEGORY, course.getCategoryName());
        response.put(MetricExportConstant.COLUMN_DATE_ENROLLED, course.getRegistrationDate());
        response.put(MetricExportConstant.COLUMN_DATE_COMPLETED, course.getCompletionDate());
        response.put(MetricExportConstant.COLUMN_USER_COURSE_RATING, course.getRating());
        response.put(MetricExportConstant.COLUMN_NUMBER_OF_LESSONS_COMPLETED, String.valueOf(course.getNumCompleted()));
        response.put(MetricExportConstant.COLUMN_TOTAL_LESSONS, String.valueOf(course.getTotalLessons()));

        addCommonDetails(response, course.getCreatedDate(), course.getPortalName());
        addPersonalDetails(
            response,
            course.getFirstName(),
            course.getLastName(),
            course.getEmail(),
            StringUtils.EMPTY,
            course.getBusinessName()
        );
        return response;
    }

    /**
     * Maps course-specific fields.
     */
    private Map<String, String> mapInvoiceFields(IResponseInvoiceExport invoice) {
        Map<String, String> response = new HashMap<>();

        // Add course-specific details
        response.put(MetricExportConstant.COLUMN_INVOICE_NUMBER, invoice.getInvoiceNumber());
        response.put(MetricExportConstant.COLUMN_HOURS_COMPLETED, invoice.getHoursCompleted());
        response.put(MetricExportConstant.COLUMN_RATE, invoice.getContractedRate());
        response.put(MetricExportConstant.COLUMN_COST, invoice.getCost());
        response.put(MetricExportConstant.COLUMN_ADVISOR, invoice.getAdvisor());
        response.put(MetricExportConstant.COLUMN_BUSINESS_OWNER_NAME, invoice.getBusinessOwnerName());
        response.put(MetricExportConstant.COLUMN_BUSINESS_NAME, invoice.getBusinessName());
        response.put(MetricExportConstant.COLUMN_APPOINTMENT_DATE, invoice.getAppointmentDate());
        response.put(MetricExportConstant.COLUMN_PROJECT_COMPLETION_DATE, invoice.getProjectCompletionDate());
        response.put(MetricExportConstant.COLUMN_CATEGORY, invoice.getCategoryName());
        response.put(MetricExportConstant.COLUMN_SERVICE, invoice.getServiceName());
        response.put(MetricExportConstant.COLUMN_REPORT, invoice.getReportNumber());

        return response;
    }

    /**
     * Extracted method: Map events to a structured data format
     *
     * @param eventExports List<IResponseEventRegistrationExport>
     * @return List<Map < String, String>>
     */
    private List<Map<String, String>> mapEventsToData(List<IResponseEventRegistrationExport> eventExports) {
        return eventExports.stream().map(this::mapEventsFields).collect(Collectors.toList());
    }

    /**
     * Maps event-specific fields.
     */
    private Map<String, String> mapEventsFields(IResponseEventRegistrationExport event) {
        Map<String, String> response = new HashMap<>();

        // Add course-specific details
        response.put(MetricExportConstant.COLUMN_EVENT_NAME, event.getSubject());
        response.put(MetricExportConstant.COLUMN_EVENT_DESCRIPTION, event.getDescription());
        response.put(MetricExportConstant.COLUMN_EVENT_START_DATE, event.getStartTime());

        addCommonDetails(response, event.getCreatedDate(), event.getPortalName());
        addPersonalDetails(
            response,
            event.getFirstName(),
            event.getLastName(),
            event.getEmail(),
            StringUtils.EMPTY,
            event.getBusinessName()
        );
        return response;
    }

    private Map<String, String> getBusinessOwnerData(IResponseAssignAdvisor businessOwner, Portal portal, String timezone) {
        Map<String, String> answers = new HashMap<>();

        // Fetch standard and additional intake answers
        answers.putAll(fetchStandardIntakeAnswers(businessOwner, timezone));
        answers.putAll(fetchAdditionalIntakeAnswers(businessOwner, portal, timezone));
        if (Objects.nonNull(businessOwner.getApplicationId())) {
            answers.putAll(fetchAnswers(
                userFormService.getAnswerUserByEntryFormId(businessOwner.getUserId(), businessOwner.getApplicationId(), false),
                timezone
            ));
        }
        // Populate additional details
        populateBusinessOwnerDetails(answers, businessOwner, portal);

        return answers;
    }

    private Map<String, String> fetchStandardIntakeAnswers(IResponseAssignAdvisor businessOwner, String timezone) {
        var ignoredQuestions = Set.of(
            FormConstant.PORTAL_INTAKE_QUESTION_FIRST_NAME,
            FormConstant.PORTAL_INTAKE_QUESTION_LAST_NAME,
            FormConstant.PORTAL_INTAKE_QUESTION_EMAIL_ADDRESS,
            FormConstant.PORTAL_INTAKE_QUESTION_PHONE_NUMBER
        );

        var relevantQuestions = MetricExportConstant.LIST_QUESTION_BUSINESS_OWNER_INTAKES_EXPORT.stream()
            .filter(question -> !ignoredQuestions.contains(question))
            .toList();

        return fetchAnswers(userFormService.getListAnswerUserByQuestionCode(businessOwner.getUserId(), relevantQuestions, false), timezone);
    }

    private Map<String, String> fetchAdditionalIntakeAnswers(IResponseAssignAdvisor businessOwner, Portal portal, String timezone) {
        return fetchAnswers(
            userFormService.getListAnswerAdditionalQuestionUserByFormCode(
                businessOwner.getUserId(),
                portal.getId(),
                FormCodeEnum.PORTAL_INTAKE_ADDITIONAL_QUESTION,
                false
            ),
            timezone
        );
    }

    private void populateBusinessOwnerDetails(Map<String, String> answers, IResponseAssignAdvisor businessOwner, Portal portal) {
        addCommonDetails(
            answers,
            DateUtils.convertInstantToStringTime(businessOwner.getCreatedDate(), DateTimeFormat.YYYY_MM_DD),
            portal.getPlatformName()
        );
        addPersonalDetails(
            answers,
            businessOwner.getFirstName(),
            businessOwner.getLastName(),
            businessOwner.getEmail(),
            businessOwner.getPhoneNumber(),
            null
        );
    }

    private Map<String, String> getTechnicalAssistanceApplicationData(
        IResponseInfoApplication application,
        Portal portal,
        String timezone
    ) {
        Map<String, String> answers = fetchAnswers(
            userFormService.getAnswerUserByEntryFormId(application.getUserId(), application.getApplicationId(), false),
            timezone
        );

        // Populate additional details
        addCommonDetails(
            answers,
            DateUtils.convertInstantToStringTime(application.getCreatedDate(), DateTimeFormat.YYYY_MM_DD),
            portal.getPlatformName()
        );
        addPersonalDetails(
            answers,
            application.getFirstName(),
            application.getLastName(),
            application.getEmail(),
            application.getPhoneNumber(),
            null
        );

        answers.put(MetricExportConstant.COLUMN_NAME_YOUR_BUSINESS, getBusinessName(application.getUserId()));

        return answers;
    }

    private void addCommonDetails(Map<String, String> answers, String createdDate, String portalName) {
        answers.put(MetricExportConstant.COLUMN_PORTAL_NAME, portalName);
        answers.put(MetricExportConstant.COLUMN_CREATED_DATE, createdDate);
    }

    private void addPersonalDetails(
        Map<String, String> answers,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String businessName
    ) {
        answers.put(MetricExportConstant.COLUMN_FIRST_NAME, firstName);
        answers.put(MetricExportConstant.COLUMN_LAST_NAME, lastName);
        answers.put(MetricExportConstant.COLUMN_YOUR_EMAIL, email);
        answers.put(MetricExportConstant.COLUMN_YOUR_PHONE_NUMBER, phoneNumber);
        if (Objects.nonNull(businessName)) {
            answers.put(MetricExportConstant.COLUMN_NAME_YOUR_BUSINESS, businessName);
        }
    }

    private Map<String, String> fetchAnswers(List<ResponseUserAnswerForm> responses, String timezone) {
        return responses
            .stream()
            .filter(response -> Objects.nonNull(response.getQuestion()) && Objects.nonNull(response.getAnswer()))
            .peek(ele -> {
                if (QuestionTypeEnum.DATE_PICKER.equals(ele.getQuestionType())) {
                    ele.setAnswer(
                        DateUtils.convertInstantToStringTime(Instant.parse(ele.getAnswer()), DateTimeFormat.YYYY_MM_DD, timezone)
                    );
                }
            })
            .collect(
                Collectors.toMap(ele ->
                    appendAnswer(ele.getQuestionCode(), ele.getQuestion()),
                    ResponseUserAnswerForm::getAnswer,
                    (existing, replacement) -> existing,
                    LinkedHashMap::new
                )
            );
    }

    private String getBusinessName(UUID userId) {
        return userAnswerFormRepository
            .findByQuestionCodeAndEntryIdAndEntryType(PORTAL_INTAKE_QUESTION_BUSINESS, userId, EntryTypeEnum.USER)
            .map(UserAnswerForm::getAdditionalAnswer)
            .orElse(null);
    }

    private <T> List<Map<String, String>> mapData(List<T> data, Function<T, Map<String, String>> mapper) {
        return Optional.ofNullable(data).orElse(List.of()).stream().map(mapper).toList();
    }

    private List<Map<String, String>> mapTechnicalAssistanceData(List<IResponseInfoApplication> data, Portal portal, String timezone) {
        return mapData(data, application -> getTechnicalAssistanceApplicationData(application, portal, timezone));
    }

    private List<Map<String, String>> mapBusinessOwnerData(List<IResponseAssignAdvisor> data, Portal portal, String timezone) {
        return mapData(data, businessOwner -> getBusinessOwnerData(businessOwner, portal, timezone));
    }

    private List<String> getHeaders(
        UUID portalId,
        List<String> initialHeaders,
        List<String> standardQuestions,
        FormCodeEnum additionalQuestionFormCode
    ) {
        List<String> headers = new ArrayList<>(initialHeaders);

        // Add standard intake questions
        String questionCodesStr = String.join(",", standardQuestions);
        headers.addAll(getQuestionsFromRepository(questionCodesStr));

        // Add additional portal-specific intake questions
        headers.addAll(getQuestionsFromRepository(portalId, additionalQuestionFormCode));

        return headers;
    }

    private List<String> getTechnicalAssistanceApplicationHeaders(UUID portalId) {
        return getHeaders(
            portalId,
            MetricExportConstant.LIST_HEADER_COMMON_PERSONAL_STANDARD_EXPORT,
            MetricExportConstant.LIST_QUESTION_TECHNICAL_ASSISTANCE_EXPORT,
            FormCodeEnum.TECHNICAL_ASSISTANCE_ADDITIONAL_QUESTION
        );
    }

    private List<String> getBusinessOwnerHeaders(UUID portalId) {
        return getHeaders(
            portalId,
            List.of(MetricExportConstant.COLUMN_PORTAL_NAME, MetricExportConstant.COLUMN_CREATED_DATE),
            MetricExportConstant.LIST_QUESTION_BUSINESS_OWNER_INTAKES_EXPORT,
            FormCodeEnum.PORTAL_INTAKE_ADDITIONAL_QUESTION
        );
    }
}
