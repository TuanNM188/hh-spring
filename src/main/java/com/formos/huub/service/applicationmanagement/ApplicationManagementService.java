package com.formos.huub.service.applicationmanagement;

import com.formos.huub.domain.constant.ActiveCampaignConstant;
import com.formos.huub.domain.constant.BusinessConstant;
import com.formos.huub.domain.constant.FormConstant;
import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.entity.embedkey.TechnicalAssistanceAdvisorEmbedKey;
import com.formos.huub.domain.enums.ApprovalStatusEnum;
import com.formos.huub.domain.enums.FormCodeEnum;
import com.formos.huub.domain.enums.LocationTypeEnum;
import com.formos.huub.domain.request.technicalassistance.RequestApprovalApplication;
import com.formos.huub.domain.request.technicalassistance.RequestSearchTechnicalApplicationSubmit;
import com.formos.huub.domain.request.technicalassistance.RequestUpdateTechnicalAssistance;
import com.formos.huub.domain.response.answerform.IResponseQuestion;
import com.formos.huub.domain.response.answerform.ResponseUserAnswerForm;
import com.formos.huub.domain.response.answerform.ResponseViewAnswer;
import com.formos.huub.domain.response.member.ResponsePortalByRole;
import com.formos.huub.domain.response.portals.IResponseLocation;
import com.formos.huub.domain.response.technicaladvisor.IResponseTechnicalAdvisorInfo;
import com.formos.huub.domain.response.technicalassistance.*;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.exception.NotFoundException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.helper.member.MemberHelper;
import com.formos.huub.mapper.user.UserMapper;
import com.formos.huub.repository.*;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.activecampaign.ActiveCampaignStrategy;
import com.formos.huub.service.member.MemberService;
import com.formos.huub.service.pushnotification.PushNotificationService;
import com.formos.huub.service.useranswerform.UserFormService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationManagementService {


    TechnicalAssistanceSubmitRepository technicalAssistanceSubmitRepository;

    LocationRepository locationRepository;

    UserMapper userMapper;

    UserFormService userFormService;

    QuestionRepository questionRepository;

    UserRepository userRepository;

    ActiveCampaignStrategy activeCampaignStrategy;

    ProgramTermVendorRepository programTermVendorRepository;

    CommunityPartnerRepository communityPartnerRepository;

    PortalRepository portalRepository;

    TechnicalAssistanceAdvisorRepository technicalAssistanceAdvisorRepository;

    TechnicalAdvisorRepository technicalAdvisorRepository;

    AppointmentRepository appointmentRepository;

    ProjectRepository projectRepository;

    PushNotificationService pushNotificationService;

    MemberService memberService;

    MemberHelper memberHelper;

    /**
     * search All Application With Projects by condition
     *
     * @param request RequestSearchTechnicalApplicationSubmit
     * @return Map<String, Object> response
     */
    public Map<String, Object> searchAllApplicationWithProjects(RequestSearchTechnicalApplicationSubmit request) {
        String sort = ObjectUtils.isEmpty(request.getSort()) ? "tas.submit_at,desc" : request.getSort();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        request.setPortalIds(getListPortalByRole(request.getPortalId()));
        HashMap<String, String> sortFieldMappings = new HashMap<>();
        sortFieldMappings.put("businessOwnerName", "u.normalized_full_name");
        sortFieldMappings.put("applicationStatus", "tas.status");
        sortFieldMappings.put("platformName", "p.platform_name");
        sortFieldMappings.put("lastAppointment", "lastAp.lastAppointment");
        sortFieldMappings.put("upcomingAppointment", "upComing.upcomingAppointment");
        sortFieldMappings.put("projectStatus", "pro.projectStatus");
        sortFieldMappings.put("remainingAwardHours", "tas.remaining_award_hours");
        sortFieldMappings.put(BusinessConstant.TIMEZONE_KEY, request.getTimezone());
        // Convert search conditions based on field mappings
        request.setSearchConditions(ObjectUtils.convertSortParams(request.getSearchConditions(), sortFieldMappings));

        request.setExcludeStatus(getExcludeStatus());
        request.setCommunityPartnerId(getCurrentCommunityPartnerId());
        // Fetch data and map to response DTOs
        var data = technicalAssistanceSubmitRepository.getAllApplicationProjectWithPageable(request, pageable);

        // Return paginated response
        return PageUtils.toPage(data);
    }

    public IResponseCountApplication getTechnicalAssistanceOverview(UUID portalId) {
        var portalIds = getListPortalByRole(portalId);
        var communityPartnerId = getCurrentCommunityPartnerId();
        return technicalAssistanceSubmitRepository.countByPortalIdAndStatus(portalIds, communityPartnerId);
    }

    public List<IResponseInfoApplication> getAllTechnicalAssistanceApproved(UUID portalId) {
        var portalIds = getListPortalByRole(portalId);
        var communityPartnerId = getCurrentCommunityPartnerId();
        return technicalAssistanceSubmitRepository.getAllApprovedApplicationByPortal(portalIds, communityPartnerId);
    }

    public List<IResponseInfoApplication> getAllTechnicalAssistance(UUID programTermId, UUID userId) {
        return technicalAssistanceSubmitRepository.getAllApplicationByPortalAndUser(programTermId, userId);
    }

    /**
     * get Data application Detail by ID
     *
     * @param applicationId UUID
     * @return ResponseApplicationDetail object response
     */
    public ResponseApplicationDetail getApplicationDetail(UUID applicationId) {
        var technicalAssistance = getTechnicalAssistanceSubmit(applicationId);
        var user = technicalAssistance.getUser();
        var portal = Optional.ofNullable(technicalAssistance.getPortal())
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Portal")));

        var responseBuilder = ResponseApplicationDetail.builder()
            .id(technicalAssistance.getId())
            .status(technicalAssistance.getStatus())
            .approvalDate(technicalAssistance.getApprovalDate())
            .basicInformation(basicInfoBusinessOwner(user))
            .technicalAssistance(getInfoTechnicalAssistance(user.getId(), technicalAssistance))
            .portalId(portal.getId());

        addDeniedReasonIfApplicable(technicalAssistance, responseBuilder);
        addApprovalDetailsIfApplicable(technicalAssistance, responseBuilder);
        addProgramTermIfAvailable(technicalAssistance, responseBuilder);
        addBusinessOwnerDetails(user, portal, responseBuilder);

        return responseBuilder.build();
    }

    /**
     * process Approval Application by portal host or admin
     *
     * @param request RequestApprovalApplication
     */
    public void processApprovalApplication(RequestApprovalApplication request) {
        var technicalAssistance = getTechnicalAssistance(request.getApplicationId());

        if (!ApprovalStatusEnum.SUBMITTED.equals(technicalAssistance.getStatus())) {
            throw new NotFoundException(MessageHelper.getMessage(Message.Keys.E0056));
        }

        //Handle process denied application
        if (ApprovalStatusEnum.DENIED.equals(request.getStatus())) {
            processDeniedApplication(technicalAssistance, request.getReason());
        }

        //Handle process approved application
        if (ApprovalStatusEnum.VENDOR_ASSIGNED.equals(request.getStatus())) {
            processApprovedApplication(technicalAssistance, request);
        }
    }

    public void revertToPending(UUID technicalAssistanceSubmitId) {
        var technicalAssistance = getTechnicalAssistance(technicalAssistanceSubmitId);
        if (!isDenied(technicalAssistance)) {
            return;
        }
        validateProgramTerm(technicalAssistance);
        validateNoActiveApplication(technicalAssistance);
        resetToSubmitted(technicalAssistance);
    }


    public List<IResponseTermVendor> getTermVendorByTermId(UUID termId) {
        return programTermVendorRepository.findAllByProgramTermId(termId);
    }

    public void updateTechnicalAssistance(UUID technicalAssistanceId, RequestUpdateTechnicalAssistance request) {
        var technicalAssistanceSubmit = getTechnicalAssistanceSubmit(technicalAssistanceId);
        validateAssignAwardHours(request.getAssignAwardHours());

        var termVendor = getVendorById(request.getVendorId());
        UUID preVendorId = technicalAssistanceSubmit.getAssignVendorId();

        if (isVendorChanged(preVendorId, request.getVendorId())) {
            if (checkUseApplication(technicalAssistanceId, null)) {
                throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0093));
            }
            adjustPreviousVendorHours(preVendorId, technicalAssistanceSubmit.getAssignAwardHours());
        } else {
            validateAndAllocateHours(termVendor, technicalAssistanceSubmit.getAssignAwardHours(), request.getAssignAwardHours());
        }

        validateUsedHours(technicalAssistanceSubmit, request.getAssignAwardHours());

        updateTechnicalAssistanceDetails(technicalAssistanceSubmit, request);
        updateAssignedAdvisors(technicalAssistanceId, technicalAssistanceSubmit, request.getAssignAdvisorIds());

        technicalAssistanceSubmitRepository.save(technicalAssistanceSubmit);
    }

    private void addDeniedReasonIfApplicable(TechnicalAssistanceSubmit technicalAssistance, ResponseApplicationDetail.ResponseApplicationDetailBuilder builder) {
        if (ApprovalStatusEnum.DENIED.equals(technicalAssistance.getStatus())) {
            builder.deniedReason(technicalAssistance.getReason());
        }
    }

    private void addApprovalDetailsIfApplicable(TechnicalAssistanceSubmit technicalAssistance, ResponseApplicationDetail.ResponseApplicationDetailBuilder builder) {
        if (EnumSet.of(ApprovalStatusEnum.APPROVED, ApprovalStatusEnum.VENDOR_ASSIGNED).contains(technicalAssistance.getStatus())) {
            builder.assignAwardHours(technicalAssistance.getAssignAwardHours())
                .hoursRemaining(technicalAssistance.getRemainingAwardHours());

            Optional.ofNullable(technicalAssistance.getAssignVendorId())
                .flatMap(communityPartnerRepository::findByVendorId)
                .ifPresent(vendor -> builder.communityPartnerName(vendor.getName()));
        }
    }

    private void addProgramTermIfAvailable(TechnicalAssistanceSubmit technicalAssistance, ResponseApplicationDetail.ResponseApplicationDetailBuilder builder) {
        Optional.ofNullable(technicalAssistance.getProgramTerm())
            .ifPresent(programTerm -> builder.programTermId(programTerm.getId()));
    }

    private void addBusinessOwnerDetails(User user, Portal portal, ResponseApplicationDetail.ResponseApplicationDetailBuilder builder) {
        UUID userId = user.getId();
        UUID portalId = portal.getId();

        builder.businessInformation(getBusinessOwnerIntakeByFormCode(userId, portalId, FormCodeEnum.PORTAL_INTAKE_QUESTION_BUSINESS))
            .demographics(getBusinessOwnerIntakeByFormCode(userId, portalId, FormCodeEnum.PORTAL_INTAKE_QUESTION_DEMOGRAPHICS))
            .businessNeeds(getBusinessOwnerIntakeByFormCode(userId, portalId, FormCodeEnum.PORTAL_INTAKE_QUESTION_ASSISTANCE_NEEDS))
            .additionalQuestions(getAnswerAdditionalQuestionByFormCode(userId, portalId));
    }

    private TechnicalAssistanceSubmit getTechnicalAssistance(UUID id) {
        return technicalAssistanceSubmitRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(
                MessageHelper.getMessage(Message.Keys.E0010, "Application")));
    }

    private boolean isDenied(TechnicalAssistanceSubmit technicalAssistance) {
        return ApprovalStatusEnum.DENIED.equals(technicalAssistance.getStatus());
    }

    private void validateProgramTerm(TechnicalAssistanceSubmit technicalAssistance) {
        if (Boolean.FALSE.equals(technicalAssistance.getProgramTerm().getIsCurrent())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0094));
        }
    }

    private void validateNoActiveApplication(TechnicalAssistanceSubmit technicalAssistance) {
        var businessOwnerUser = technicalAssistance.getUser();
        var programTermId = technicalAssistance.getProgramTerm().getId();

        boolean hasActiveApplication = technicalAssistanceSubmitRepository.existsByApplicationActive(
            businessOwnerUser.getId(), programTermId);

        if (hasActiveApplication) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0094));
        }
    }

    private void resetToSubmitted(TechnicalAssistanceSubmit technicalAssistance) {
        technicalAssistance.setStatus(ApprovalStatusEnum.SUBMITTED);
        technicalAssistance.setReason(null);
        technicalAssistanceSubmitRepository.save(technicalAssistance);

        syncValueActiveCampaignApplicationApproval(technicalAssistance.getUser().getId(), null, null, ApprovalStatusEnum.SUBMITTED);
    }

    private TechnicalAssistanceSubmit getTechnicalAssistanceSubmit(UUID technicalAssistanceId) {
        return technicalAssistanceSubmitRepository.findById(technicalAssistanceId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Application")));
    }

    private void validateAssignAwardHours(Float assignAwardHours) {
        if (assignAwardHours == null || assignAwardHours < 0) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0018, "Assign Award Hours", "Invalid value"));
        }
    }

    private ProgramTermVendor getVendorById(UUID vendorId) {
        return programTermVendorRepository.findById(vendorId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Vendor")));
    }

    private boolean isVendorChanged(UUID preVendorId, UUID newVendorId) {
        return preVendorId != null && !preVendorId.equals(newVendorId);
    }

    private void adjustPreviousVendorHours(UUID preVendorId, Float assignAwardHours) {
        var preVendor = getVendorById(preVendorId);
        preVendor.setAllocatedHours(preVendor.getAllocatedHours() - assignAwardHours);
    }

    private void validateAndAllocateHours(ProgramTermVendor termVendor, Float previousAwardHours, Float newAwardHours) {
        float availableHours = termVendor.getCalculatedHours()
            - Optional.ofNullable(termVendor.getAllocatedHours()).orElse(0f)
            + previousAwardHours;

        if (newAwardHours > availableHours) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0018, "Assign Award Hours", "Available Hours"));
        }
        updateAllocatedHours(termVendor.getId(), previousAwardHours, newAwardHours, termVendor, true);
    }

    private void validateUsedHours(TechnicalAssistanceSubmit technicalAssistanceSubmit, Float newAwardHours) {
        float usedHours = Optional.ofNullable(technicalAssistanceSubmit.getAssignAwardHours()).orElse(0f)
            - Optional.ofNullable(technicalAssistanceSubmit.getRemainingAwardHours()).orElse(0f);

        if (usedHours > newAwardHours) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0018, "Assign Award Hours", "Award Hours Used"));
        }
    }

    private void updateTechnicalAssistanceDetails(TechnicalAssistanceSubmit technicalAssistanceSubmit, RequestUpdateTechnicalAssistance request) {
        float usedHours = Optional.ofNullable(technicalAssistanceSubmit.getAssignAwardHours()).orElse(0f)
            - Optional.ofNullable(technicalAssistanceSubmit.getRemainingAwardHours()).orElse(0f);

        technicalAssistanceSubmit.setAssignAwardHours(request.getAssignAwardHours());
        technicalAssistanceSubmit.setAssignVendorId(request.getVendorId());
        technicalAssistanceSubmit.setRemainingAwardHours(request.getAssignAwardHours() - usedHours);
    }

    private void updateAssignedAdvisors(UUID technicalAssistanceId, TechnicalAssistanceSubmit technicalAssistanceSubmit, List<UUID> assignAdvisorIds) {

        var preAdvisorIds = technicalAssistanceAdvisorRepository.getAllAdvisorByTechnicalAssistance(technicalAssistanceId).stream()
            .map(IResponseTechnicalAdvisorInfo::getTechnicalAdvisorId).toList();
        technicalAssistanceAdvisorRepository.deleteById_TechnicalAssistanceSubmit_Id(technicalAssistanceId);

        List<String> advisorNames = new ArrayList<>();
        CommunityPartner communityPartner = fetchCommunityPartnerIfPresent(technicalAssistanceSubmit.getAssignVendorId());
        String assignedVendorName = Optional.ofNullable(communityPartner).map(CommunityPartner::getName).orElse(null);

        if (!ObjectUtils.isEmpty(assignAdvisorIds)) {
            if (assignAdvisorIds.size() > BusinessConstant.NUMBER_2) {
                throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0095));
            }
            List<TechnicalAssistanceAdvisor> advisors = technicalAdvisorRepository.findAllById(assignAdvisorIds).stream()
                .map(advisor -> {
                    var user = advisor.getUser();
                    if (Objects.nonNull(user)) {
                        advisorNames.add(user.getNormalizedFullName());
                    }
                    return createTechnicalAssistanceAdvisor(advisor, technicalAssistanceSubmit);
                })
                .collect(Collectors.toList());
            technicalAssistanceSubmit.setStatus(ApprovalStatusEnum.APPROVED);
            var diffObjects = ObjectUtils.getDifferenceList(preAdvisorIds, assignAdvisorIds);
            if (!ObjectUtils.isEmpty(diffObjects.getAdded()) || !ObjectUtils.isEmpty(diffObjects.getRemoved())) {
                pushNotificationService.sendMailAssignAdvisorApplicationForBusinessOwner(technicalAssistanceSubmit, communityPartner, advisorNames);
            }
            technicalAssistanceAdvisorRepository.saveAll(advisors);
        }
        syncApproveApplicationToActiveCampaign(technicalAssistanceSubmit.getUser().getId(), advisorNames, assignedVendorName, technicalAssistanceSubmit.getAssignAwardHours());
    }

    /**
     * Sync Approve Application To ActiveCampaign
     *
     * @param userId UUID
     * @param advisorNames List<String>
     * @param assignedVendor String
     * @param awardHours Float
     */
    private void syncApproveApplicationToActiveCampaign(UUID userId, List<String> advisorNames, String assignedVendor, Float awardHours) {
        User user = memberHelper.getUserById(userId);
        if (Objects.isNull(user)) {
            return;
        }

        Map<String, String> campaignValueMap = new HashMap<>();
        campaignValueMap.put(ActiveCampaignConstant.FIELD_TA_STATUS_V2, ActiveCampaignConstant.APPROVED);
        campaignValueMap.put(ActiveCampaignConstant.FIELD_ASSIGNED_VENDOR_V2, assignedVendor);
        campaignValueMap.put(ActiveCampaignConstant.FIELD_AWARD_AMOUNT_V2, Objects.nonNull(awardHours) ? awardHours.toString() : null );

        String advisor1 = null;
        String advisor2 = null;

        if (Objects.nonNull(advisorNames) && !advisorNames.isEmpty()) {
            advisor1 = advisorNames.getFirst();
            advisor2 = advisorNames.size() > BusinessConstant.NUMBER_1 ? advisorNames.getLast() : null;
        }

        campaignValueMap.put(ActiveCampaignConstant.FIELD_ADVISOR_1_V2, advisor1);
        campaignValueMap.put(ActiveCampaignConstant.FIELD_ADVISOR_2_V2, advisor2);

        activeCampaignStrategy.syncValueActiveCampaignApplication(user, campaignValueMap);
    }

    private TechnicalAssistanceAdvisor createTechnicalAssistanceAdvisor(TechnicalAdvisor advisor, TechnicalAssistanceSubmit technicalAssistanceSubmit) {

        var id = new TechnicalAssistanceAdvisorEmbedKey();
        id.setTechnicalAdvisor(advisor);
        id.setTechnicalAssistanceSubmit(technicalAssistanceSubmit);
        var taAdvisor = new TechnicalAssistanceAdvisor();
        taAdvisor.setId(id);

        return taAdvisor;
    }


    /**
     * get Info Technical Assistance By ApplicationId
     *
     * @param applicationId UUID
     * @return ResponseInfoTechnicalAssistance
     */
    public ResponseInfoTechnicalAssistance getInfoTechnicalAssistanceByTerm(UUID applicationId) {
        var technicalAssistance = technicalAssistanceSubmitRepository
            .findById(applicationId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0016, "Application")));

        var responseBuilder = ResponseInfoTechnicalAssistance.builder()
            .applicationStatus(technicalAssistance.getStatus())
            .technicalAssistanceId(technicalAssistance.getId())
            .assignAwardHours(technicalAssistance.getAssignAwardHours())
            .remainingAwardHours(technicalAssistance.getRemainingAwardHours())
            .deniedReason(technicalAssistance.getReason())
            .advisors(getAdvisorAssignForTechnicalAssistance(technicalAssistance.getId()));

        Optional.ofNullable(technicalAssistance.getAssignVendorId()).ifPresent(
            vendorId -> responseBuilder.vendor(getInfoVendorByVendorId(vendorId))
        );

        return responseBuilder.build();
    }

    public Boolean checkUseApplication(UUID applicationId, UUID technicalAdvisorId) {
        if (Objects.isNull(applicationId)) {
            return null;
        }
        var isAppointmentUse = appointmentRepository.existsByTechnicalAssistanceSubmitId(applicationId, technicalAdvisorId);
        var isProjectUse = projectRepository.existsByTechnicalAssistanceSubmitId(applicationId, technicalAdvisorId);
        return isAppointmentUse || isProjectUse;
    }

    public List<UUID> getListPortalByRole(UUID portalId) {
        if (Objects.nonNull(portalId)) {
            return List.of(portalId);
        }
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.SYSTEM_ADMINISTRATOR)) {
            return List.of();
        }
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.COMMUNITY_PARTNER)) {
            return getCommunityPartnerPortals();
        }
        return List.of(Objects.requireNonNull(PortalContextHolder.getPortalId()));
    }

    public List<UUID> getListPortalByRole(UUID portalId, boolean isPortalAdvisor) {
        if (Objects.nonNull(portalId)) {
            return List.of(portalId);
        }
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.SYSTEM_ADMINISTRATOR)) {
            return List.of();
        }

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.COMMUNITY_PARTNER)) {
            return getCommunityPartnerPortals();
        }
        if (isPortalAdvisor && SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.TECHNICAL_ADVISOR)) {
            return getTechnicalAdvisorPortals();
        }
        return List.of(Objects.requireNonNull(PortalContextHolder.getPortalId()));
    }

    private List<UUID> getCommunityPartnerPortals() {
        return Optional.ofNullable(SecurityUtils.getCurrentUser(userRepository))
            .map(User::getCommunityPartner)
            .map(communityPartner -> portalRepository.getPortalByCommunityPartnerId(communityPartner.getId()))
            .map(portals -> portals.stream().map(IResponseLocation::getId).toList())
            .orElse(List.of());
    }

    private List<UUID> getTechnicalAdvisorPortals() {
        return Optional.ofNullable(SecurityUtils.getCurrentUser(userRepository))
            .map(user -> memberService.getPortalByUser(user.getId(), true))
            .map(portals -> portals.stream().map(ResponsePortalByRole::getId).toList())
            .orElse(List.of());
    }

    private UUID getCurrentCommunityPartnerId() {
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.COMMUNITY_PARTNER)) {
            return null;
        }
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        return Optional.ofNullable(currentUser.getCommunityPartner()).map(CommunityPartner::getId).orElse(null);
    }

    private List<ResponseInfoAdvisor> getAdvisorAssignForTechnicalAssistance(UUID technicalAssistanceId) {
        return technicalAssistanceAdvisorRepository
            .getAllAdvisorByTechnicalAssistance(technicalAssistanceId)
            .stream()
            .map(
                ele ->
                    ResponseInfoAdvisor.builder()
                        .userId(ele.getUserId())
                        .technicalAdvisorId(ele.getTechnicalAdvisorId())
                        .fullName(ele.getFullName())
                        .imageUrl(ele.getImageUrl())
                        .build()
            )
            .toList();
    }

    private ResponseInfoVendor getInfoVendorByVendorId(UUID vendorId) {
        var communityPartnerOpt = communityPartnerRepository.findByVendorId(vendorId);
        var vendorOpt = programTermVendorRepository.findById(vendorId);

        if (vendorOpt.isEmpty() || communityPartnerOpt.isEmpty()) {
            log.warn("Vendor or Community Partner not found for vendorId: {}", vendorId);
            return null;
        }

        var vendor = vendorOpt.get();
        var communityPartner = communityPartnerOpt.get();
        var response = new ResponseInfoVendor();
        response.setVendorId(vendorId);
        response.setCommunityPartnerId(communityPartner.getId());
        response.setCommunityPartnerName(communityPartner.getName());
        response.setCalculatedHours(vendor.getCalculatedHours() - Optional.ofNullable(vendor.getAllocatedHours()).orElse(0f));
        userRepository
            .findCommunityPartnerNavigatorById(communityPartner.getId())
            .ifPresent(navigator -> {
                response.setNavigatorId(navigator.getId());
                response.setNavigatorName(navigator.getNormalizedFullName());
                response.setImageUrl(navigator.getImageUrl());
            });
        return response;
    }


    private String getExcludeStatus() {
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.COMMUNITY_PARTNER)) {
            return null;
        }
        return String.join(",", ApprovalStatusEnum.SUBMITTED.getValue(), ApprovalStatusEnum.DENIED.getValue());
    }

    /**
     * process Denied Application by portal host or admin
     *
     * @param technicalAssistanceSubmit TechnicalAssistanceSubmit
     * @param reason                    denied reason
     */
    private void processDeniedApplication(TechnicalAssistanceSubmit technicalAssistanceSubmit, String reason) {
        if (ObjectUtils.isEmpty(reason)) {
            throw new NotFoundException(MessageHelper.getMessage(Message.Keys.E0016, "Denied reason"));
        }
        var approver = getCurrentUser();
        technicalAssistanceSubmit.setStatus(ApprovalStatusEnum.DENIED);
        technicalAssistanceSubmit.setReason(reason);
        technicalAssistanceSubmit.setApprover(approver);
        technicalAssistanceSubmit.setApprovalDate(Instant.now());
        technicalAssistanceSubmitRepository.save(technicalAssistanceSubmit);
        pushNotificationService.sendMailProcessDeniedForBusinessOwner(technicalAssistanceSubmit);
        syncValueActiveCampaignApplicationApproval(technicalAssistanceSubmit.getUser().getId(), null, null, ApprovalStatusEnum.DENIED);
    }

    /**
     * process Approved Application by portal host or admin
     *
     * @param technicalAssistanceSubmit TechnicalAssistanceSubmit
     * @param request                   RequestApprovalApplication
     */
    private void processApprovedApplication(TechnicalAssistanceSubmit technicalAssistanceSubmit, RequestApprovalApplication request) {
        var approver = getCurrentUser();
        CommunityPartner communityPartner = fetchCommunityPartnerIfPresent(request.getAssignVendorId());
        String assignedVendor = Optional.ofNullable(communityPartner).map(CommunityPartner::getName).orElse(null);
        var termVendor = programTermVendorRepository.findById(request.getAssignVendorId())
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Term vendor")));
        if (Objects.isNull(termVendor.getCalculatedHours()) || request.getAssignAwardHours() > termVendor.getCalculatedHours()) {
            throw new NotFoundException(MessageHelper.getMessage(Message.Keys.E0083));
        }

        setUpTechnicalAssistanceSubmit(technicalAssistanceSubmit, request, approver);
        technicalAssistanceSubmitRepository.save(technicalAssistanceSubmit);

        updateAllocatedHours(technicalAssistanceSubmit.getAssignVendorId(), BusinessConstant.NUMBER_0.floatValue(), technicalAssistanceSubmit.getAssignAwardHours(), termVendor, false);

        sendApprovalEmails(technicalAssistanceSubmit, communityPartner);
        processSyncAwardHoursToActiveCampaign(technicalAssistanceSubmit, assignedVendor);
    }

    private void updateAllocatedHours(UUID vendorId, Float oldAssignAwardHours, Float assignAwardHours, ProgramTermVendor vendor, boolean isUpdate) {
        if (vendorId == null || assignAwardHours == null || assignAwardHours <= 0) {
            return;
        }

        float currentAllocatedHours = Optional.ofNullable(vendor.getAllocatedHours()).orElse(BusinessConstant.NUMBER_0.floatValue());
        float updatedHours = isUpdate
            ? (currentAllocatedHours - oldAssignAwardHours + assignAwardHours)
            : (currentAllocatedHours + assignAwardHours);
        vendor.setAllocatedHours(updatedHours);
        programTermVendorRepository.save(vendor);
    }

    private void setUpTechnicalAssistanceSubmit(
        TechnicalAssistanceSubmit technicalAssistanceSubmit,
        RequestApprovalApplication request,
        User approver
    ) {
        technicalAssistanceSubmit.setStatus(ApprovalStatusEnum.VENDOR_ASSIGNED);
        technicalAssistanceSubmit.setApprover(approver);
        technicalAssistanceSubmit.setApprovalDate(Instant.now());
        technicalAssistanceSubmit.setAssignAwardHours(request.getAssignAwardHours());
        technicalAssistanceSubmit.setRemainingAwardHours(request.getAssignAwardHours());
        if (Objects.nonNull(request.getAssignVendorId())) {
            technicalAssistanceSubmit.setAssignVendorId(request.getAssignVendorId());
        }
    }

    private CommunityPartner fetchCommunityPartnerIfPresent(UUID assignVendorId) {
        if (Objects.nonNull(assignVendorId)) {
            return communityPartnerRepository
                .findByVendorId(assignVendorId)
                .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Community Partner")));
        }
        return null;
    }

    private void sendApprovalEmails(TechnicalAssistanceSubmit technicalAssistanceSubmit, CommunityPartner communityPartner) {
        pushNotificationService.sendMailApprovedForBusinessOwner(technicalAssistanceSubmit, communityPartner);
        pushNotificationService.sendMailApprovedForCommunityPartnerNavigator(technicalAssistanceSubmit, communityPartner);
    }

    private void processSyncAwardHoursToActiveCampaign(TechnicalAssistanceSubmit technicalAssistanceSubmit, String assignedVendor) {
        var awardHours = technicalAssistanceSubmit.getAssignAwardHours();
        var vendorId = technicalAssistanceSubmit.getAssignVendorId();

        if (Objects.nonNull(awardHours) && Objects.nonNull(vendorId)) {
            var programTermVendor = programTermVendorRepository
                .findById(vendorId)
                .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Vendor")));
            var awardValue = BigDecimal.valueOf(awardHours).multiply(programTermVendor.getContractedRate());
            syncValueActiveCampaignApplicationApproval(
                technicalAssistanceSubmit.getUser().getId(),
                awardHours,
                assignedVendor,
                technicalAssistanceSubmit.getStatus()
            );
        }
    }

    /**
     * get Info Technical Assistance by user
     *
     * @param userId                    UUID
     * @param technicalAssistanceSubmit TechnicalAssistanceSubmit
     * @return List<ResponseViewAnswer> response value
     */
    private List<ResponseViewAnswer> getInfoTechnicalAssistance(UUID userId, TechnicalAssistanceSubmit technicalAssistanceSubmit) {
        // Fetch all user answers for the specified form
        List<ResponseUserAnswerForm> allAnswers = userFormService.getAnswerUserByEntryFormId(userId, technicalAssistanceSubmit.getId(), true);

        // Separate and sort technical assistance answers
        List<ResponseUserAnswerForm> technicalAssistanceAnswers = allAnswers
            .stream()
            .filter(
                answer ->
                    !answer.getQuestionCode().contains(FormConstant.QUESTION_OTHER) &&
                        !answer.getQuestionCode().equals(FormConstant.PORTAL_INTAKE_ADDITIONAL_QUESTION)
            )
            .sorted(Comparator.comparing(ResponseUserAnswerForm::getPriorityOrder))
            .toList();

        // Separate and sort additional answers
        List<ResponseUserAnswerForm> additionalAnswers = allAnswers
            .stream()
            .filter(
                answer ->
                    !answer.getQuestionCode().contains(FormConstant.QUESTION_OTHER) &&
                        answer.getQuestionCode().equals(FormConstant.PORTAL_INTAKE_ADDITIONAL_QUESTION)
            )
            .sorted(Comparator.comparing(ResponseUserAnswerForm::getPriorityOrder))
            .toList();

        // Combine both lists and map to response objects
        return Stream.concat(technicalAssistanceAnswers.stream(), additionalAnswers.stream()).map(this::mapToResponseViewAnswer).toList();
    }

    /**
     * get Business Owner Intake By FormCode
     *
     * @param userId   UUID
     * @param portalId UUID
     * @param formCode FormCodeEnum
     * @return List<ResponseViewAnswer> list object response
     */
    private List<ResponseViewAnswer> getBusinessOwnerIntakeByFormCode(UUID userId, UUID portalId, FormCodeEnum formCode) {
        var questionCodes = questionRepository
            .getAllByFormCodeAndPortalId(formCode, portalId, List.of())
            .stream()
            .map(IResponseQuestion::getQuestionCode)
            .toList();
        return userFormService
            .getListAnswerUserByQuestionCode(userId, questionCodes, true)
            .stream()
            .map(this::mapToResponseViewAnswer)
            .sorted(Comparator.comparing(ResponseViewAnswer::getPriorityOrder))
            .toList();
    }

    /**
     * get Answer Additional Question By Form Code
     *
     * @param userId   UUID
     * @param portalId UUID
     * @return List<ResponseViewAnswer>
     */
    private List<ResponseViewAnswer> getAnswerAdditionalQuestionByFormCode(UUID userId, UUID portalId) {
        return userFormService
            .getListAnswerAdditionalQuestionUserByFormCode(userId, portalId, FormCodeEnum.PORTAL_INTAKE_ADDITIONAL_QUESTION, true)
            .stream()
            .map(this::mapToResponseViewAnswer)
            .sorted(Comparator.comparing(ResponseViewAnswer::getPriorityOrder))
            .toList();
    }

    /**
     * map To Response View Answer
     *
     * @param userAnswerForm ResponseUserAnswerForm
     * @return ResponseViewAnswer
     */
    private ResponseViewAnswer mapToResponseViewAnswer(ResponseUserAnswerForm userAnswerForm) {
        var item = new ResponseViewAnswer();
        BeanUtils.copyProperties(userAnswerForm, item);
        return item;
    }

    /**
     * Get basic Info Business Owner
     *
     * @param user User
     * @return ResponseBasicInfoBusinessOwner ọbject response
     */
    private ResponseBasicInfoBusinessOwner basicInfoBusinessOwner(User user) {
        // Map user to basic info response
        ResponseBasicInfoBusinessOwner response = userMapper.toResponseBasicInfoBusinessOwner(user);
        response.setAddress(user.getAddress1());
        // Set state name if available
        Optional.ofNullable(response.getState())
            .flatMap(stateCode -> locationRepository.findOneByCodeAndLocationType(stateCode, LocationTypeEnum.STATE))
            .ifPresent(state -> response.setState(state.getName()));

        // Set country name if available
        Optional.ofNullable(response.getCountry())
            .flatMap(countryCode -> locationRepository.findAllByGeoNameIdAndLocationType(countryCode, LocationTypeEnum.COUNTRY))
            .ifPresent(country -> response.setCountry(country.getName()));

        // Retrieve business-related answers
        List<String> questionCodes = List.of(
            FormConstant.PORTAL_INTAKE_QUESTION_BUSINESS,
            FormConstant.PORTAL_INTAKE_QUESTION_PREFER_CONTACTED
        );

        Map<String, String> answers = userFormService.getAnswerUserByQuestionCode(user.getId(), questionCodes);
        response.setNameOfYourBusiness(answers.get(FormConstant.PORTAL_INTAKE_QUESTION_BUSINESS));
        response.setPreferToBeContacted(answers.get(FormConstant.PORTAL_INTAKE_QUESTION_PREFER_CONTACTED));

        return response;
    }

    /**
     * sync Value Active Campaign Application Approval
     *
     * @param userId       UUID
     * @param awardHours Float
     * @param assignedVendor String
     */
    private void syncValueActiveCampaignApplicationApproval(UUID userId, Float awardHours, String assignedVendor, ApprovalStatusEnum status) {
        User user = memberHelper.getUserById(userId);
        if (Objects.isNull(user)) {
            return;
        }

        // Build contact request payload
        Map<String, String> campaignValueMap = new HashMap<>();
        if (ApprovalStatusEnum.APPROVED.equals(status)) {
            campaignValueMap.put(ActiveCampaignConstant.FIELD_AWARD_AMOUNT_V2, Objects.nonNull(awardHours) ? awardHours.toString() : null );
            campaignValueMap.put(ActiveCampaignConstant.FIELD_ASSIGNED_VENDOR_V2, assignedVendor);
        }
        campaignValueMap.put(ActiveCampaignConstant.FIELD_TA_STATUS_V2, ActiveCampaignConstant.TA_STATUS.get(status.getValue()));
        // Sync values with ActiveCampaign
        activeCampaignStrategy.syncValueActiveCampaignApplication(user, campaignValueMap);
    }


    private User getCurrentUser() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin).orElse(null);
    }
}
