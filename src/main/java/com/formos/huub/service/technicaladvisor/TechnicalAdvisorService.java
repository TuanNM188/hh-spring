package com.formos.huub.service.technicaladvisor;

import com.formos.huub.domain.constant.ActiveCampaignConstant;
import com.formos.huub.domain.constant.BusinessConstant;
import com.formos.huub.domain.constant.FormConstant;
import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.*;
import com.formos.huub.domain.request.appointmentmanagement.RequestRescheduleAppointment;
import com.formos.huub.domain.request.technicaladvisor.RequestBookingAppointment;
import com.formos.huub.domain.request.technicaladvisor.RequestListTechnicalAdvisor;
import com.formos.huub.domain.request.technicaladvisor.RequestSearchAdvisor;
import com.formos.huub.domain.request.technicaladvisor.RequestUpdateTechnicalAdvisor;
import com.formos.huub.domain.response.advisementcategory.IResponseAdvisementCategory;
import com.formos.huub.domain.response.advisementcategory.IResponseCountAdvisor;
import com.formos.huub.domain.response.advisementcategory.ResponseCountAdvisorUsing;
import com.formos.huub.domain.response.communitypartner.IResponseAdvisorByAppointment;
import com.formos.huub.domain.response.communitypartner.IResponseAssignAdvisor;
import com.formos.huub.domain.response.specialty.IResponseSpecialtyUser;
import com.formos.huub.domain.response.technicaladvisor.*;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.constant.AppConstants;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.helper.member.MemberHelper;
import com.formos.huub.mapper.appointment.AppointmentMapper;
import com.formos.huub.mapper.bookingsetting.BookingSettingMapper;
import com.formos.huub.mapper.technicaladvisor.TechnicalAdvisorMapper;
import com.formos.huub.repository.*;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.activecampaign.ActiveCampaignStrategy;
import com.formos.huub.service.portals.PortalFormService;
import com.formos.huub.service.pushnotification.PushNotificationService;
import com.formos.huub.service.useranswerform.UserFormService;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TechnicalAdvisorService extends BaseService {

    TechnicalAdvisorRepository technicalAdvisorRepository;
    UserRepository userRepository;
    CacheManager cacheManager;
    InviteRepository inviteRepository;
    TechnicalAdvisorMapper technicalAdvisorMapper;
    TechnicalAssistanceSubmitRepository technicalAssistanceSubmitRepository;
    SpecialtyUserRepository specialtyUserRepository;
    AdvisementCategoryRepository advisementCategoryRepository;
    UserFormService userFormService;
    BookingSettingMapper bookingSettingMapper;
    AppointmentMapper appointmentMapper;
    AppointmentRepository appointmentRepository;
    AppointmentDetailRepository appointmentDetailRepository;
    CommunityPartnerRepository communityPartnerRepository;
    ActiveCampaignStrategy activeCampaignStrategy;
    LanguageRepository languageRepository;
    ProjectRepository projectRepository;
    PortalFormService portalFormService;
    BusinessOwnerRepository businessOwnerRepository;
    PushNotificationService pushNotificationService;
    MemberHelper memberHelper;

    private static final Integer TWO_HOURS = 2;

    /**
     * Get all TAs with pageable
     *
     * @param request RequestListTechnicalAdvisor
     * @return Map<String, Object>
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAllTechnicalAdvisorsWithPageable(RequestListTechnicalAdvisor request, String timezone) {
        var portalId = PortalContextHolder.getPortalId();
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "u.normalized_full_name,asc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        HashMap<String, String> sortMap = new HashMap<>();
        sortMap.put("normalizedFullName", "u.normalized_full_name");
        sortMap.put("email", "u.email");
        sortMap.put("organization", "u.organization");
        sortMap.put("phoneNumber", "u.phone_number");
        sortMap.put("status", "u.status");
        sortMap.put("startDate", "ta.created_date");
        sortMap.put(BusinessConstant.TIMEZONE_KEY, timezone);
        request.setSearchConditions(ObjectUtils.convertSortParams(request.getSearchConditions(), sortMap));

        return PageUtils.toPage(technicalAdvisorRepository.getAllTechnicalAdvisorsWithPageable(request, portalId, pageable));
    }

    /**
     * search Browse Advisors With Pageable
     *
     * @param request RequestSearchAdvisor
     * @return Map<String, Object>
     */
    public Map<String, Object> searchBrowseAdvisorsWithPageable(RequestSearchAdvisor request) {
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "u.last_name,asc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));

        if (Objects.isNull(request.getPortalId())) {
            request.setPortalId(PortalContextHolder.getPortalId());
        }
        UUID communityPartnerId = getCommunityPartnerIdAssignApplication(request.getTechnicalAssistanceId());
        request.setSearchKeyword(StringUtils.wildcards(request.getSearchKeyword()));
        if (Boolean.TRUE.equals(request.getIsCurrentApplication())){
            request.setTechnicalAssistanceId(getCurrentApplicationId());
        }
        return PageUtils.toPage(technicalAdvisorRepository.searchBrowseAdvisorByConditions(request, communityPartnerId, pageable));
    }

    public ResponseCountAdvisorUsing getCountAdvisorUsing(UUID applicationId, UUID communityPartnerId) {
        UUID portalId = PortalContextHolder.getPortalId();
        if (Objects.isNull(communityPartnerId)) {
            communityPartnerId = getCommunityPartnerIdAssignApplication(applicationId);
        }
        UUID finalCommunityPartnerId = communityPartnerId;
        Map<String, Integer> mapUsingCategory = fetchAdvisorCounts(
            () -> advisementCategoryRepository.countAdvisorUsingCategory(portalId, finalCommunityPartnerId, applicationId)
        );
        Map<String, Integer> mapUsingServices = fetchAdvisorCounts(
            () -> advisementCategoryRepository.countAdvisorUsingService(portalId, finalCommunityPartnerId, applicationId)
        );
        Map<String, Integer> mapUsingSpecialties = fetchAdvisorCounts(
            () -> specialtyUserRepository.countAdvisorUsingSpecialty(portalId, finalCommunityPartnerId, applicationId)
        );
        Map<String, Integer> mapUsingAreas = fetchAdvisorCounts(
            () -> specialtyUserRepository.countAdvisorUsingSpecialtyArea(portalId, finalCommunityPartnerId, applicationId)
        );
        Map<String, Integer> mapUsingCommunityPartners = fetchAdvisorCounts(
            () -> communityPartnerRepository.countAdvisorUsingCommunityPartner(portalId, finalCommunityPartnerId, applicationId)
        );
        Map<String, Integer> mapUsingLanguages = fetchAdvisorCounts(
            () -> languageRepository.countAdvisorUsingLanguage(portalId, finalCommunityPartnerId, applicationId)
        );

        var responseBuilder = ResponseCountAdvisorUsing.builder()
            .usingCategories(mapUsingCategory)
            .usingServices(mapUsingServices)
            .usingSpecialties(mapUsingSpecialties)
            .usingAreas(mapUsingAreas)
            .usingCommunityPartners(mapUsingCommunityPartners)
            .usingLanguages(mapUsingLanguages);
        var currentApplication = getApplicationCurrentUser();
        if (Objects.nonNull(currentApplication)) {
            responseBuilder.applicationStatus(currentApplication.getStatus()).assignVendorId(currentApplication.getAssignVendorId());
        }
        return responseBuilder.build();
    }

    private Map<String, Integer> fetchAdvisorCounts(Supplier<List<IResponseCountAdvisor>> supplier) {
        return supplier
            .get()
            .stream()
            .collect(Collectors.toMap(entry -> entry.getId().toString(), IResponseCountAdvisor::getNumAdvisor, Integer::sum));
    }

    public ResponseDetailBrowseAdvisor getDetailBrowseAdvisor(UUID technicalAdvisorId) {
        var technicalAdvisor = getTechnicalAdvisor(technicalAdvisorId);
        var user = technicalAdvisor.getUser();
        var userId = user.getId();

        var response = technicalAdvisorMapper.toResponseAdvisor(user);
        response.setUserId(userId);
        response.setTechnicalAdvisorId(technicalAdvisorId);
        setAdvisorDetails(response, userId);

        Optional.ofNullable(PortalContextHolder.getPortalId()).ifPresent(portalId -> {
            Optional.ofNullable(portalFormService.getCurrentTerm(portalId)).ifPresent(currentTerm -> {
                response.setProgramTermId(currentTerm.getId());
                setTechnicalAssistanceDetails(response, portalId, currentTerm.getId());
            });
        });

        return response;
    }

    private void setAdvisorDetails(ResponseDetailBrowseAdvisor response, UUID userId) {
        response.setAreas(getDistinctListFromSpecialtyUsers(userId, IResponseSpecialtyUser::getSpecialtyAreaNames));
        response.setServices(getDistinctListFromCategories(userId, IResponseAdvisementCategory::getServiceNames));
        response.setCategories(getDistinctNamesFromCategories(userId));
        response.setSpecialties(getDistinctNamesFromSpecialtyUsers(userId));
        response.setLanguages(getLanguagesSpoken(userId));
    }

    private void setTechnicalAssistanceDetails(ResponseDetailBrowseAdvisor response, UUID portalId, UUID programTermId) {
        var currentUserId = SecurityUtils.getCurrentUser(userRepository).getId();
        technicalAssistanceSubmitRepository
            .findByPortalIdAndUserIdAndProgramTermId(portalId, currentUserId, programTermId)
            .ifPresent(data -> {
                response.setApply11Status(data.getStatus());
                response.setAssignAwardHours(data.getAssignAwardHours());
                response.setRemainingAwardHours(data.getRemainingAwardHours());

                communityPartnerRepository
                    .findByVendorId(data.getAssignVendorId())
                    .ifPresent(cp -> response.setCommunityPartnerId(cp.getId()));
            });
    }

    public ResponseBookingAppointmentForm getBookingAppointmentAdvisorForm(UUID technicalAdvisorId) {
        var technicalAdvisor = getTechnicalAdvisor(technicalAdvisorId);
        var user = technicalAdvisor.getUser();
        var bookingSetting = user.getBookingSetting();
        if (Objects.isNull(bookingSetting)) {
            return null;
        }
        var appointmentBooked = appointmentRepository
            .getAppointmentAdvisor(Instant.now(), technicalAdvisorId)
            .stream()
            .map(appointmentMapper::toResponseAppointmentBooked)
            .toList();
        var response = bookingSettingMapper.toResponseSetting(bookingSetting);
        response.setAppointmentBooked(appointmentBooked);
        return response;
    }

    /**
     * booking Appointment Advisor
     *
     * @param request RequestBookingAppointment
     * @return ResponseBookingAppointmentAdvisor
     */
    public ResponseBookingAppointmentAdvisor rescheduleAppointment(
        UUID appointmentId,
        RequestRescheduleAppointment request,
        String timezone
    ) {
        var appointment = getAppointment(appointmentId);
        if (!canReschedule(appointment)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0076));
        }
        var portal = appointment.getPortal();
        var technicalAdvisor = appointment.getTechnicalAdvisor();
        var bookingSetting = technicalAdvisor.getUser().getBookingSetting();
        var businessOwnerUser = appointment.getUser();

        checkAppointmentConflicts(request.getAppointmentDate(), businessOwnerUser.getId(), technicalAdvisor.getId());

        appointmentMapper.partialUpdate(appointment, request);
        appointment.setTimezone(timezone);
        appointmentRepository.save(appointment);
        pushNotificationService.sendRescheduleAppointmentMailForBusinessOwner(appointment);
        pushNotificationService.sendRescheduleAppointmentMailApprovedForAdvisor(appointment);
        syncValueActiveCampaignBookingAppointment(businessOwnerUser.getId(), bookingSetting, request.getAppointmentDate(), portal.getId(), false);
        return buildResponse(appointment, businessOwnerUser);
    }

    public ResponseBookingAppointmentAdvisor bookingAppointmentAdvisor(RequestBookingAppointment request, String timezone) {
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        var businessOwner = getBusinessOwner(request, currentUser);
        var businessOwnerUser = getBusinessOwnerUser(businessOwner);
        var portalId = Objects.requireNonNullElse(request.getPortalId(), PortalContextHolder.getPortalId());

        var technicalAssistanceSubmit = validateBookingSettingForm(businessOwner, portalId);
        float timeUseForAppointment = BusinessConstant.NUMBER_1;

        validateRemainingAwardHours(technicalAssistanceSubmit, timeUseForAppointment);
        //        checkAppointmentConflicts(request, businessOwnerUser.getId());

        var technicalAdvisor = getTechnicalAdvisor(request.getTechnicalAdvisorId());
        //        validateTechnicalAdvisorBooking(request, technicalAdvisor);

        var appointment = createAndSaveAppointment(request, businessOwnerUser, technicalAdvisor, technicalAssistanceSubmit, timezone);
        createAndSaveAppointmentDetail(request, appointment, technicalAssistanceSubmit, timeUseForAppointment);

        boolean applicationCompleted = BigDecimal.valueOf(technicalAssistanceSubmit.getRemainingAwardHours()).compareTo(BigDecimal.ZERO) == 0;

        syncWithActiveCampaign(businessOwnerUser.getId(), technicalAdvisor, request.getAppointmentDate(), portalId, applicationCompleted);

        return buildResponse(appointment, businessOwnerUser);
    }

    private User getBusinessOwnerUser(BusinessOwner businessOwner) {
        return userRepository
            .findById(businessOwner.getUser().getId())
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Business Owner")));
    }

    private void syncWithActiveCampaign(UUID userId, TechnicalAdvisor technicalAdvisor, Instant appointmentDate, UUID portalId, boolean applicationCompleted) {
        syncValueActiveCampaignBookingAppointment(
            userId,
            technicalAdvisor.getUser().getBookingSetting(),
            appointmentDate,
            portalId,
            applicationCompleted
        );
    }

    private ResponseBookingAppointmentAdvisor buildResponse(Appointment appointment, User businessOwnerUser) {
        var response = appointmentMapper.toResponseBookingAppointmentAdvisor(appointment);
        response.setBusinessOwnerName(businessOwnerUser.getNormalizedFullName());
        response.setBusinessOwnerId(businessOwnerUser.getId());
        response.setEmail(businessOwnerUser.getEmail());
        response.setPhoneNumber(businessOwnerUser.getPhoneNumber());
        return response;
    }

    private BusinessOwner getBusinessOwner(RequestBookingAppointment request, User currentUser) {
        var errorMessage = MessageHelper.getMessage(Message.Keys.E0010, "Business Owner");

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.BUSINESS_OWNER)) {
            return businessOwnerRepository.findByUserId(currentUser.getId()).orElseThrow(() -> new BadRequestException(errorMessage));
        }
        UUID businessOwnerId = request.getBusinessOwnerId();
        if (businessOwnerId == null) {
            throw new BadRequestException(errorMessage);
        }
        return businessOwnerRepository.findById(businessOwnerId).orElseThrow(() -> new BadRequestException(errorMessage));
    }

    private void validateRemainingAwardHours(TechnicalAssistanceSubmit technicalAssistanceSubmit, float timeUseForAppointment) {
        if (
            Objects.isNull(technicalAssistanceSubmit.getAssignAwardHours()) ||
            technicalAssistanceSubmit.getRemainingAwardHours() < timeUseForAppointment
        ) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0078));
        }
    }

    private boolean canReschedule(Appointment appointment) {
        // Check condition: current time < (appointment time - 2 hours)
        return Instant.now().isBefore(appointment.getAppointmentDate().minusSeconds(TWO_HOURS * AppConstants.SECONDS_OF_A_HOUR));
    }

    private Appointment getAppointment(UUID id) {
        return appointmentRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Appointment")));
    }

    private void checkAppointmentConflicts(Instant appointmentDate, UUID businessOwnerId, UUID technicalAdvisorId) {
        boolean isAdvisorConflict = appointmentRepository.isExistAppointmentAdvisor(appointmentDate, technicalAdvisorId);
        boolean isBusinessOwnerConflict = appointmentRepository.isExistAppointmentAdBusinessOwner(appointmentDate, businessOwnerId);

        if (isAdvisorConflict || isBusinessOwnerConflict) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0069));
        }
    }

    /**
     * sync Value Active Campaign Booking Appointment
     *
     * @param userId UUID
     */
    private void syncValueActiveCampaignBookingAppointment(
        UUID userId,
        BookingSetting bookingSetting,
        Instant appointmentDate,
        UUID portalId,
        boolean applicationCompleted
    ) {
        User user = memberHelper.getUserById(userId);
        // Build contact request payload
        if (Objects.isNull(portalId)) {
            return;
        }
        var booking = appointmentRepository.countByUserIdAndPortalId(user.getId(), portalId);
        var dateOfWeeks = bookingSetting
            .getAvailabilities()
            .stream()
            .map(ele -> DayOfWeek.valueOf(ele.getDaysOfWeek().getValue()))
            .toList();
        var nextBookingDate = getNextDateFromList(dateOfWeeks, appointmentDate, bookingSetting.getTimezone());
        Map<String, String> campaignValueMap = new HashMap<>();
        campaignValueMap.put(ActiveCampaignConstant.FIELD_SCHEDULED_APPOINTMENT_COUNT_V2, booking.toString());
        if (applicationCompleted) {
            campaignValueMap.put(ActiveCampaignConstant.FIELD_TA_STATUS_V2, ActiveCampaignConstant.COMPLETED);
        }
        // Sync values with ActiveCampaign
        activeCampaignStrategy.syncValueActiveCampaignApplication(user, campaignValueMap);
    }

    public String getNextDateFromList(List<DayOfWeek> days, Instant startInstant, String timezone) {
        if (ObjectUtils.isEmpty(days) || Objects.isNull(startInstant)) {
            return StringUtils.EMPTY;
        }

        List<DayOfWeek> sortedDays = days.stream().sorted(Comparator.comparingInt(DayOfWeek::getValue)).toList();

        LocalDate startDate = startInstant.atZone(ZoneId.of(timezone)).toLocalDate();
        DayOfWeek currentDay = startDate.getDayOfWeek();

        Optional<DayOfWeek> nextDay = sortedDays.stream().filter(day -> day.getValue() > currentDay.getValue()).findFirst();
        return nextDay.map(startDate::with).orElseGet(() -> startDate.with(sortedDays.getFirst()).plusWeeks(1)).toString();
    }

    private Appointment createAndSaveAppointment(
        RequestBookingAppointment request,
        User businessOwner,
        TechnicalAdvisor technicalAdvisor,
        TechnicalAssistanceSubmit technicalAssistanceSubmit,
        String timezone
    ) {
        var portal = technicalAssistanceSubmit.getPortal();
        var communityPartner = fetchCommunityPartner(technicalAssistanceSubmit.getAssignVendorId());
        var appointment = appointmentMapper.toEntity(request);
        appointment.setCommunityPartner(communityPartner);
        appointment.setTechnicalAdvisor(technicalAdvisor);
        appointment.setStatus(AppointmentStatusEnum.SCHEDULED);
        appointment.setUser(businessOwner);
        appointment.setPortal(portal);
        appointment.setTechnicalAssistanceSubmit(technicalAssistanceSubmit);
        appointment.setTimezone(timezone);
        return appointmentRepository.save(appointment);
    }

    private void createAndSaveAppointmentDetail(
        RequestBookingAppointment request,
        Appointment appointment,
        TechnicalAssistanceSubmit technicalAssistanceSubmit,
        float timeUseForAppointment
    ) {
        var appointmentDetail = new AppointmentDetail();
        BeanUtils.copyProperties(request, appointmentDetail);
        appointmentDetail.setAppointment(appointment);

        appointmentDetail.setUseAwardHours(BusinessConstant.NUMBER_1.floatValue());
        appointmentDetailRepository.save(appointmentDetail);

        pushNotificationService.sendScheduleAppointmentMailForAdvisor(appointment);
        pushNotificationService.sendScheduleAppointmentMailForBusinessOwner(appointment);

        updateRemainingHoursApplications(technicalAssistanceSubmit, timeUseForAppointment);
    }

    private void updateRemainingHoursApplications(TechnicalAssistanceSubmit technicalAssistanceSubmit, Float timeUseForAppointment) {
        float remainingHours;
        if (Objects.nonNull(technicalAssistanceSubmit.getRemainingAwardHours())) {
            remainingHours = technicalAssistanceSubmit.getRemainingAwardHours() - timeUseForAppointment;
        } else {
            remainingHours = technicalAssistanceSubmit.getAssignAwardHours() - timeUseForAppointment;
        }
        technicalAssistanceSubmit.setRemainingAwardHours(remainingHours);
        technicalAssistanceSubmitRepository.save(technicalAssistanceSubmit);
    }

    private TechnicalAssistanceSubmit validateBookingSettingForm(BusinessOwner businessOwner, UUID portalId) {
        var businessOwnerUser = businessOwner.getUser();

        var currentTerm = portalFormService.getCurrentTerm(portalId);
        if (Objects.isNull(currentTerm)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0015, "Current Program Term"));
        }
        return technicalAssistanceSubmitRepository
            .findByPortalIdAndUserIdAndProgramTermId(portalId, businessOwnerUser.getId(), currentTerm.getId())
            .filter(technicalAssistanceSubmit -> ApprovalStatusEnum.APPROVED.equals(technicalAssistanceSubmit.getStatus()))
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0015, "Technical Assistance")));
    }

    private TechnicalAssistanceSubmit getApplicationCurrentUser() {
        var currentUser = SecurityUtils.getCurrentUser(userRepository);

        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.BUSINESS_OWNER)) {
            return null;
        }
        var portalId = PortalContextHolder.getPortalId();
        var currentTerm = portalFormService.getCurrentTerm(portalId);
        if (Objects.isNull(currentTerm)) {
            return null;
        }
        return technicalAssistanceSubmitRepository
            .findByPortalIdAndUserIdAndProgramTermId(portalId, currentUser.getId(), currentTerm.getId())
            .orElse(null);
    }

    private TechnicalAdvisor getTechnicalAdvisor(UUID id) {
        return technicalAdvisorRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Technical Advisor")));
    }

    private CommunityPartner fetchCommunityPartner(UUID assignVendorId) {
        return communityPartnerRepository
            .findByVendorId(assignVendorId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Community Partner")));
    }

    /**
     * Get TA detail
     *
     * @param id UUID
     * @return IResponseTechnicalAdvisorDetail
     */
    @Transactional(readOnly = true)
    public IResponseTechnicalAdvisorDetail getTechnicalAdvisorById(UUID id) {
        return technicalAdvisorRepository.getTechnicalAdvisorById(id);
    }

    /**
     * update TA
     *
     * @param id      UUID
     * @param request RequestUpdateTechnicalAdvisor
     */
    public void updateTechnicalAdvisor(UUID id, RequestUpdateTechnicalAdvisor request) {
        TechnicalAdvisor technicalAdvisor = technicalAdvisorRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Technical Advisor")));
        Optional<User> userOpt = userRepository
            .findOneByEmailIgnoreCase(request.getEmail())
            .filter(user -> !user.getId().equals(technicalAdvisor.getUser().getId()));
        if (userOpt.isPresent()) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "Email"));
        }
        User user = technicalAdvisor.getUser();
        BeanUtils.copyProperties(request, user);
        BeanUtils.copyProperties(request, technicalAdvisor);
        user.setLogin(request.getEmail());
        user.makeNormalizedFullName();
        user.setStatus(request.getProfileStatus());
        technicalAdvisorRepository.save(technicalAdvisor);
        insertUserSettings(user.getId());
        this.clearUserCaches(user);
    }


    /**
     * get Events for technical advisor
     *
     * @param technicalAdvisorId UUID
     * @return List<ResponseTechnicalAdvisorEvent>
     */
    public List<ResponseTechnicalAdvisorEvent> getEvents(UUID technicalAdvisorId) {
        List<ResponseTechnicalAdvisorEvent> events = new ArrayList<>();

        // TODO fake data test
        LocalDateTime baseStartTime = LocalDateTime.of(2024, 9, 4, 10, 0);
        for (int i = 1; i <= 50; i++) {
            LocalDateTime startTime = baseStartTime.plusHours(i * 9);
            LocalDateTime endTime = startTime.plusHours(2);
            ResponseTechnicalAdvisorEvent event = ResponseTechnicalAdvisorEvent.builder()
                .id(UUID.randomUUID())
                .title("Test event number: " + i)
                .start(startTime.toInstant(ZoneOffset.UTC))
                .end(endTime.toInstant(ZoneOffset.UTC))
                .avatar(i % 2 == 0 ? "https://cellphones.com.vn/sforum/wp-content/uploads/2024/02/anh-avatar-cute-58.jpg" : "https://cellphones.com.vn/sforum/wp-content/uploads/2024/02/anh-avatar-cute-84.jpg")
                .linkMeetingPlatform("https://zoom.us/j/12345678" + i)
                .meetingPlatform(i % 2 == 0 ? MeetingPlatformEnum.GOOGLE_MEET : MeetingPlatformEnum.ZOOM_VIDEO).build();
            events.add(event);
        }
        return events;
    }
    /**
     * Verify Invite Technical Advisor Link
     *
     * @param inviteToken String
     */
    public void verifyInviteTechnicalAdvisorLink(String inviteToken) {
        Invite invite = inviteRepository
            .findByInviteToken(inviteToken)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0034)));

        if (Instant.now().isAfter(invite.getExpiresAt())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0035));
        }
    }

    /**
     * Insert User Settings
     *
     * @param userId UUID
     */
    public void insertUserSettings(UUID userId) {
        userRepository.insertUserSettings(userId);
    }

    private void clearUserCaches(User user) {
        Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)).evict(user.getLogin());
        if (user.getEmail() != null) {
            Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE)).evict(user.getEmail());
        }
    }

    /**
     * get Community Partner Id assigned For Business Owner
     *
     * @return UUID
     */
    private UUID getCommunityPartnerIdAssignApplication(UUID applicationId) {
        if (Objects.nonNull(applicationId)) {
            return technicalAssistanceSubmitRepository
                .findById(applicationId)
                .map(TechnicalAssistanceSubmit::getAssignVendorId)
                .flatMap(communityPartnerRepository::findByVendorId)
                .map(CommunityPartner::getId)
                .orElse(null);
        }
        UUID portalId = PortalContextHolder.getPortalId();
        if (portalId == null || !SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.BUSINESS_OWNER)) {
            return null;
        }
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        var currentTerm = portalFormService.getCurrentTerm(portalId);
        if (currentTerm == null) {
            return null;
        }
        return technicalAssistanceSubmitRepository
            .findByPortalIdAndUserIdAndStatus(
                currentTerm.getId(),
                currentUser.getId(),
                List.of(ApprovalStatusEnum.VENDOR_ASSIGNED.getValue(), ApprovalStatusEnum.APPROVED.getValue())
            )
            .map(TechnicalAssistanceSubmit::getAssignVendorId)
            .flatMap(communityPartnerRepository::findByVendorId)
            .map(CommunityPartner::getId)
            .orElse(null);
    }

    private UUID getCurrentApplicationId() {
        UUID portalId = PortalContextHolder.getPortalId();
        if (portalId == null || !SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.BUSINESS_OWNER)) {
            return null;
        }
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        var currentTerm = portalFormService.getCurrentTerm(portalId);
        if (currentTerm == null) {
            return null;
        }
        return technicalAssistanceSubmitRepository
            .findByPortalIdAndUserIdAndStatus(
                currentTerm.getId(),
                currentUser.getId(),
                List.of(ApprovalStatusEnum.VENDOR_ASSIGNED.getValue(), ApprovalStatusEnum.APPROVED.getValue())
            )
            .map(TechnicalAssistanceSubmit::getId)
            .orElse(null);
    }

    /**
     * Helper method to get distinct list from specialty users based on a JSON field
     *
     * @param userId         UUID
     * @param fieldExtractor Function<IResponseSpecialtyUser, String>
     * @return List<String>
     */
    private List<String> getDistinctListFromSpecialtyUsers(UUID userId, Function<IResponseSpecialtyUser, String> fieldExtractor) {
        var gson = new Gson();
        Type listType = new TypeToken<List<String>>() {}.getType();
        return specialtyUserRepository
            .getAllByUserId(userId)
            .stream()
            .map(fieldExtractor)
            .filter(ele -> !ObjectUtils.isEmpty(ele))
            .flatMap(json -> {
                var items = gson.<List<String>>fromJson(json, listType);
                return items == null ? Stream.empty() : items.stream();
            })
            .distinct()
            .toList();
    }

    /**
     * Helper method to get distinct names from specialty users
     *
     * @param userId UUID
     * @return List<String>
     */
    private List<String> getDistinctNamesFromSpecialtyUsers(UUID userId) {
        return specialtyUserRepository.getAllByUserId(userId).stream().map(IResponseSpecialtyUser::getName).distinct().toList();
    }

    /**
     * Helper method to get distinct list from categories based on a JSON field
     *
     * @param userId         UUID
     * @param fieldExtractor Function<IResponseAdvisementCategory, String>
     * @return List<String>
     */
    private List<String> getDistinctListFromCategories(UUID userId, Function<IResponseAdvisementCategory, String> fieldExtractor) {
        var gson = new Gson();
        Type listType = new TypeToken<List<String>>() {}.getType();
        return advisementCategoryRepository
            .getAllAdvisementCategoryByUserId(userId)
            .stream()
            .map(fieldExtractor)
            .filter(ele -> !ObjectUtils.isEmpty(ele))
            .flatMap(json -> {
                var items = gson.<List<String>>fromJson(json, listType);
                return items == null ? Stream.empty() : items.stream();
            })
            .distinct()
            .toList();
    }

    /**
     * Helper method to get distinct names from categories
     *
     * @param userId UUID
     * @return List<String>
     */
    private List<String> getDistinctNamesFromCategories(UUID userId) {
        return advisementCategoryRepository
            .getAllAdvisementCategoryByUserId(userId)
            .stream()
            .map(IResponseAdvisementCategory::getName)
            .distinct()
            .toList();
    }

    /**
     * Helper method to retrieve languages spoken by the user
     *
     * @param userId UUID
     * @return List<String> response
     */
    private List<String> getLanguagesSpoken(UUID userId) {
        var userLanguageMap = userFormService.getAnswerUserByQuestionCode(userId, List.of(FormConstant.QUESTION_LANGUAGE_SPOKEN));
        return Optional.ofNullable(userLanguageMap.get(FormConstant.QUESTION_LANGUAGE_SPOKEN))
            .map(lang -> List.of(lang.split(",")))
            .orElse(Collections.emptyList());
    }

    public List<IResponseAssignAdvisor> getAssignedAdvisorsByCommunityPartnerId(String communityPartnerId) {
        return technicalAdvisorRepository.getAssignedAdvisorsByCommunityPartnerId(
            UUID.fromString(communityPartnerId),
            UserStatusEnum.ACTIVE,
            FormCodeEnum.DEMOGRAPHICS,
            EntryTypeEnum.USER
        );
    }

    public IResponseAdvisorByAppointment getAdvisorByAppointmentId(String appointmentId) {
        return technicalAdvisorRepository
            .getTechnicalAdvisorByAppointmentId(UUIDUtils.convertToUUID(appointmentId))
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Appointment")));
    }
}
