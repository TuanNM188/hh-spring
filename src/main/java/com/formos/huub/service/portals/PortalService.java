package com.formos.huub.service.portals;

import com.formos.huub.domain.constant.BusinessConstant;
import com.formos.huub.domain.constant.FormConstant;
import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.FeatureCodeEnum;
import com.formos.huub.domain.enums.PortalHostStatusEnum;
import com.formos.huub.domain.enums.PortalStatusEnum;
import com.formos.huub.domain.enums.UserStatusEnum;
import com.formos.huub.domain.request.account.KeyAndPasswordRequest;
import com.formos.huub.domain.request.portals.*;
import com.formos.huub.domain.response.answerform.ResponseUserAnswerForm;
import com.formos.huub.domain.response.communitypartner.ResponseCommunityPartner;
import com.formos.huub.domain.response.feature.IResponseFeaturePortalSetting;
import com.formos.huub.domain.response.member.ResponseMetaMember;
import com.formos.huub.domain.response.portals.*;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.helper.portal.PortalHelper;
import com.formos.huub.mapper.feature.FeatureMapper;
import com.formos.huub.mapper.member.MemberMapper;
import com.formos.huub.mapper.portals.PortalMapper;
import com.formos.huub.repository.*;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.service.calendarevent.CalendarEventService;
import com.formos.huub.service.fundingservice.FundingService;
import com.formos.huub.service.invite.InviteService;
import com.formos.huub.service.learninglibrary.LearningLibraryService;
import com.formos.huub.service.useranswerform.UserFormService;
import com.formos.huub.validator.PortalValidator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.security.RandomUtil;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PortalService extends BaseService {

    PortalRepository portalRepository;


    PortalMapper portalMapper;

    ProgramRepository programRepository;

    ProgramTermRepository programTermRepository;

    ProgramTermVendorRepository programTermVendorRepository;

    FeatureRepository featureRepository;

    FeatureMapper featureMapper;

    FundingRepository fundingRepository;

    PortalStateRepository portalStateRepository;

    UserRepository userRepository;

    PortalHostRepository portalHostRepository;

    InviteService inviteService;

    PasswordEncoder passwordEncoder;

    AuthorityRepository authorityRepository;

    PortalIntakeQuestionRepository portalIntakeQuestionRepository;

    MemberRepository memberRepository;

    MemberMapper memberMapper;

    PortalValidator portalValidator;

    PortalHelper portalHelper;

    PortalFeatureRepository portalFeatureRepository;

    UserFormService userFormService;

    PortalFormService portalFormService;

    FundingService fundingService;

    LearningLibraryService learningLibraryService;

    CalendarEventService calendarEventService;

    TechnicalAssistanceSubmitRepository technicalAssistanceSubmitRepository;

    AppointmentRepository appointmentRepository;

    /**
     * search Portals
     *
     * @param request SearchPortalsRequest
     * @return Map<String, Object>
     */
    public Map<String, Object> searchPortals(RequestSearchPortals request) {
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "p.createdDate,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        HashMap<String, String> sortMap = new HashMap<>();
        sortMap.put("contractYearStartDate", "pr.contractStart");
        sortMap.put("contractYearEndDate", "pr.contractEnd");
        sortMap.put("state", "ls.name");
        request.setSearchConditions(ObjectUtils.convertSortParams(request.getSearchConditions(), sortMap));
        var data = portalRepository.searchByTermAndCondition(request, pageable);
        return PageUtils.toPage(data);
    }

    /**
     * delete Portal
     *
     * @param portalId UUID
     */
    public void deletePortal(UUID portalId) {
        boolean isCurrentlyInUse = fundingRepository.existByPortalId(portalId);
        if (isCurrentlyInUse) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0036, "Portal"));
        }
        portalStateRepository.deleteAllByPortalId(portalId);
        programTermVendorRepository.deleteByPortalId(portalId);
        programTermRepository.deleteByPortalId(portalId);
        programRepository.deleteByPortalId(portalId);
        portalIntakeQuestionRepository.deleteAllByPortalId(portalId);
        portalRepository.deleteById(portalId);
    }

    /**
     * get All Manager
     *
     * @param portalId UUID
     * @return List<ResponseMetaMember>
     */
    public List<ResponseMetaMember> getAllManager(UUID portalId) {
        return memberRepository.getAllProgramManager(portalId).stream()
            .map(ele -> ResponseMetaMember.builder().userId(ele.getId())
                .name(ele.getNormalizedFullName())
                .imageUrl(ele.getImageUrl()).build()).toList();
    }

    /**
     * check Exist Platform Name
     *
     * @param platformName String
     * @param portalId     UUID
     * @return boolean
     */
    public boolean checkExistPlatformName(String platformName, UUID portalId) {
        String oldPlatformName = null;
        if (Objects.nonNull(portalId)) {
            var portal = portalHelper.getPortal(portalId);
            oldPlatformName = portal.getPlatformName();
        }
        return portalValidator.checkExistPlatformName(platformName, oldPlatformName);
    }

    /**
     * Find detail by id
     *
     * @param id UUID
     * @return ResponsePortalDetail
     */
    public ResponsePortalDetail findDetailById(UUID id) {
        ResponsePortalDetail response;
        Portal portal = portalHelper.getPortal(id);
        response = portalMapper.toResponseDetail(portal);
        List<IResponseFeaturePortalSetting> portalFeatureSettings = featureRepository.getAllFeaturePortalSettingByPortalId(id);
        List<ResponsePortalFeature> portalFeatures = featureMapper.toListResponse(portalFeatureSettings);
        response.setPortalFeatures(portalFeatures);
        response.setPortalLocation(portalHelper.getStateOfPortal(portal));
        partialPortalHostDetail(response);
        return response;
    }

    /**
     * get About Portal
     *
     * @param id UUID
     * @return ResponseAboutPortal
     */
    public ResponseAboutPortal getAboutPortal(UUID id) {
        Portal portal = portalHelper.getPortal(id);
        ResponseAboutPortal response = portalMapper.toResponseAbout(portal);
        var portalHosts = memberRepository.getAllPortalHostInPortal(id).stream().map(memberMapper::toResponseBasic).toList();
        response.setPortalHosts(portalHosts);
        return response;
    }

    public ResponseAddressPortal getAddressPortal(UUID id) {
        Portal portal = portalHelper.getPortal(id);
        return portalMapper.toResponseAddress(portal);
    }

    /**
     * exist Portal Host
     *
     * @param email String
     * @return ResponseExistPortalHost
     */
    public ResponseExistPortalHost existPortalHost(String email) {
        return portalHelper.existPortalHost(email);
    }

    /**
     * Invites a portal host by creating an invitation or updating an existing one.
     *
     * @param request the request containing invitation details
     * @return the UUID of the invited portal host
     */
    public UUID invitePortalHost(RequestInvitePortalHost request) {
        var existPortalHost = existPortalHost(request.getEmail());
        PortalHost invitePortalHost = portalHelper.getOrCreateInvitePortalHost(request);

        if (Boolean.TRUE.equals(existPortalHost.getIsExist()) && Objects.nonNull(existPortalHost.getPortalName())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0039, existPortalHost.getPortalName()));
        }
        String inviteToken = RandomUtil.generateResetKey();
        if (Boolean.TRUE.equals(existPortalHost.getIsExist())) {
            invitePortalHost.setUserId(existPortalHost.getUserId());
            invitePortalHost.setStatus(PortalHostStatusEnum.ACTIVE);
        }
        invitePortalHost.setInviteToken(inviteToken);
        invitePortalHost.setInviteExpire(Instant.now().plus(BusinessConstant.NUMBER_90, ChronoUnit.DAYS));
        invitePortalHost = portalHostRepository.save(invitePortalHost);
        String portalName = portalHelper.getPortalNameForInvitePortalHost(request, invitePortalHost);

        var portalId = request.getPortalId();
        var portal = portalHelper.getPortal(portalId);
        portalHelper.sendInvitationEmail(invitePortalHost.getEmail(), invitePortalHost.getFirstName(), invitePortalHost.getLastName(),
            portalName, existPortalHost.getIsExist(), inviteToken, portal);

        return invitePortalHost.getId();
    }

    /**
     * create User Portal Host when active account
     *
     * @param request KeyAndPasswordRequest
     */
    public void createUserPortalHost(KeyAndPasswordRequest request) {
        PortalHost portalHost = this.verifyInviteToken(request.getKey());
        Set<Authority> authorities = new HashSet<>();
        String encryptedPassword = passwordEncoder.encode(request.getNewPassword());
        authorityRepository.findById(AuthoritiesConstants.PORTAL_HOST).ifPresent(authorities::add);

        // Create a User
        User newUser = User.builder()
            .login(portalHost.getEmail())
            .password(encryptedPassword)
            .firstName(portalHost.getFirstName())
            .lastName(portalHost.getLastName())
            .username(inviteService.generateUsername(portalHost.getFirstName(), portalHost.getLastName()))
            .email(portalHost.getEmail())
            .activated(true)
            .status(UserStatusEnum.ACTIVE)
            .authorities(authorities)
            .build();
        newUser = userRepository.save(newUser);
        insertUserSettings(newUser.getId());

        portalHost.setUserId(newUser.getId());
        portalHost.setInviteToken(null);
        portalHost.setInviteExpire(null);
        portalHost.setStatus(PortalHostStatusEnum.ACTIVE);
        portalHostRepository.save(portalHost);
    }

    /**
     * verify Invite Token
     *
     * @param inviteToken String
     * @return PortalHost Object
     */
    public PortalHost verifyInviteToken(String inviteToken) {
        PortalHost portalHost = portalHostRepository
            .findByInviteToken(inviteToken)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0034)));

        if (Instant.now().isAfter(portalHost.getInviteExpire())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0035));
        }
        return portalHost;
    }

    /**
     * Create portal
     *
     * @param request RequestCreatePortal
     * @return UUID
     */
    public UUID createPortal(RequestCreatePortal request, String origin) {
        portalValidator.validateCreate(request, origin);
        Portal portal = portalMapper.toEntity(request);
        portal = portalRepository.saveAndFlush(portal);
        portalHelper.initNavigationFeature(portal, request.getPortalFeatures());
        portalHelper.savePortalAssociations(portal, request);
        return portal.getId();
    }


    /**
     * Update portal
     *
     * @param id      UUID
     * @param request RequestUpdatePortal
     */
    public void updatePortal(UUID id, RequestUpdatePortal request, String origin) {
        Portal portal = portalHelper.getPortal(id);
        portalValidator.validateUpdate(id, portal, request, origin);

        portalMapper.partialUpdate(portal, request);
        portal = portalRepository.saveAndFlush(portal);
        portalHelper.savePortalFeatureSetting(portal, request.getPortalFeatures());
        portalHelper.savePortalAssociations(portal, request);
    }

    /**
     * update Toggle Feature
     *
     * @param portalId portalId request
     * @param request  RequestToggleFeature request object
     */
    public void updateToggleFeature(UUID portalId, RequestToggleFeature request) {
        var portalFeature = portalFeatureRepository.findById_PortalIdAndId_FeatureId(portalId, request.getFeatureId())
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Portal Feature")));
        portalFeature.setIsActive(request.getIsActive());
    }

    /**
     * get Apply 1 1 Support Screen Configurations
     *
     * @param portalId UUID request
     * @return ResponseApply11SupportScreen object
     */
    public ResponseApply11SupportScreen getApply11SupportScreenConfigurations(UUID portalId) {
        var portal = portalHelper.getPortal(portalId);
        var aboutScreenConfigurations = userFormService.getAnswerAboutScreenConfigurationByPortal(portalId);

        // Build Community Partners List
        var communityPartners = portal.getCommunityPartners().stream()
            .filter(partner -> Boolean.TRUE.equals(partner.getIsVendor()))
            .map(partner -> ResponseCommunityPartner.builder()
                .id(partner.getId())
                .name(partner.getName())
                .imageUrl(partner.getImageUrl())
                .build())
            .toList();

        var submitStatus = portalFormService.getSubmitTechnicalAssistanceStatus(portal);
        UUID programTermId = Optional.ofNullable(portalFormService.getCurrentTerm(portalId)).map(ProgramTerm::getId).orElse(null);
        // Return ResponseApply11SupportScreen
        return ResponseApply11SupportScreen.builder()
            .aboutScreenConfigurations(aboutScreenConfigurations)
            .communityPartners(communityPartners)
            .topics(Collections.emptyList())
            .primaryContactEmail(portal.getPrimaryContactEmail())
            .primaryContactName(portal.getPrimaryContactName())
            .primaryContactPhone(portal.getPrimaryContactPhone())
            .submitStatus(submitStatus)
            .programTermId(programTermId)
            .build();
    }

    /**
     * Get all location
     *
     * @return List<IResponseLocation>
     */
    public List<IResponseLocation> getAllMeta() {
        return portalRepository.getAll();
    }

    /**
     * Get portal by subdomain or custom domain
     *
     * @param subdomain      String
     * @param isCustomDomain boolean
     * @return Optional<Portal>
     */
    public Optional<Portal> getPortalBySubdomainOrCustomDomain(String subdomain, boolean isCustomDomain) {
        return portalRepository.getPortalByUrlIgnoreCaseAndIsCustomDomainAndStatus(subdomain, isCustomDomain, PortalStatusEnum.ACTIVE);
    }

    /**
     * Insert User Settings
     *
     * @param userId UUID
     */
    public void insertUserSettings(UUID userId) {
        userRepository.insertUserSettings(userId);
    }

    private void partialPortalHostDetail(ResponsePortalDetail response) {
        if (ObjectUtils.isEmpty(response.getPortalHosts())) {
            return;
        }

        // Retrieve user IDs and fetch corresponding users
        var portalHostUserIds = response.getPortalHosts()
            .stream()
            .map(ResponsePortalHost::getUserId)
            .toList();
        var portalHostUsers = userRepository.findAllById(portalHostUserIds);

        // Create a map of email to user for quick lookup
        var portalHostUserMap = ObjectUtils.convertToMap(portalHostUsers, User::getEmail);

        // Update portal hosts' status based on the user information
        response.getPortalHosts().forEach(portalHost -> {
            var user = portalHostUserMap.get(portalHost.getEmail());
            var status = Objects.nonNull(user)
                ? PortalHostStatusEnum.valueOf(user.getStatus().getValue())
                : PortalHostStatusEnum.INVITED;
            portalHost.setStatus(status);
        });
    }

    public List<IResponseWelcomeMessageSender> getWelcomeMessageSenders(String portalId) {
        UUID id = portalId.isEmpty() ? null : UUID.fromString(portalId);
        return userRepository.getWelcomeMessageSenders(id);
    }

    public ResponseMyPortalForBusinessOwner getInfoPortalForBusinessOwner(UUID portalId) {
        if (Objects.isNull(portalId)) {
            portalId = PortalContextHolder.getPortalId();
        }

        if (Objects.isNull(portalId)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Portal"));
        }
        var portal = portalHelper.getPortal(portalId);
        var responseBuilder = ResponseMyPortalForBusinessOwner.builder()
            .recommendFeature(getRecommendForBusinessOwner(portal));
        var isEnablePortalEvent = portalHelper.isEnablePortalFeature(portal.getId(), FeatureCodeEnum.PORTALS_CALENDAR_AND_EVENTS);
        if (isEnablePortalEvent) {
            var newlyEvents = calendarEventService.getNewlyEvents(portalId, 50);
            responseBuilder.newlyEvents(newlyEvents);
        }
        var isEnablePortalFunding = portalHelper.isEnablePortalFeature(portal.getId(), FeatureCodeEnum.FUNDING_DIRECTORY);
        if (isEnablePortalFunding) {
            var newlyFunding = fundingService.getNewlyFunding(portalId, 50);
            responseBuilder.newlyFunding(newlyFunding);
        }
        var isEnablePortalCourse = portalHelper.isEnablePortalFeature(portal.getId(), FeatureCodeEnum.PORTALS_LEARNING_LIBRARY);
        if (isEnablePortalCourse) {
            var newlyEvents = learningLibraryService.getNewlyCourse(portalId, 50);
            responseBuilder.newlyCourses(newlyEvents);
        }
        return responseBuilder.build();
    }

    public Boolean isUseProgramTerm(UUID programTermId) {
        if (Objects.isNull(programTermId)){
            return null;
        }
        return technicalAssistanceSubmitRepository.existsByProgramTermId(programTermId);
    }

    public Boolean isUseProgramTermVendor(UUID vendorId) {
        if (Objects.isNull(vendorId)){
            return false;
        }
        return appointmentRepository.existsByTechnicalAssistanceSubmit_AssignVendorId(vendorId);
    }

    public List<ResponseProgramTermInfo> getListProgramTermByPortal(UUID portalId){
       return   programTermRepository.findAllTermByPortalId(portalId).stream().map(ele -> {
           var item = new ResponseProgramTermInfo();
           BeanUtils.copyProperties(ele, item);
           return item;
       }).toList();
    }

    private ResponseRecommendFeature getRecommendForBusinessOwner(Portal portal) {
        return ResponseRecommendFeature.builder()
            .technicalAssistance(getInfoRecommendTechnicalAssistance(portal))
            .fundingFeature(fundingService.getRecommendFunding(portal.getId()))
            .eventFeature(calendarEventService.getRecommendEvent(portal.getId()))
            .courseFeature(learningLibraryService.getRecommendCourse(portal.getId()))
            .build();
    }

    private ResponseRecommendApplyApplication getInfoRecommendTechnicalAssistance(Portal portal) {

        var isEnablePortalProgramDetail = portalHelper.isEnablePortalFeature(portal.getId(), FeatureCodeEnum.PROGRAM_DETAILS);
        if (!isEnablePortalProgramDetail) {
            return null;
        }
        var currentTerm = portalFormService.getCurrentTerm(portal.getId());
        if (Objects.isNull(currentTerm)){
            return null;
        }
        var aboutScreenConfigurations = userFormService.getAnswerAboutScreenConfigurationByPortal(portal.getId());
        var aboutScreenConfigurationMap = ObjectUtils.convertToMap(aboutScreenConfigurations, ResponseUserAnswerForm::getQuestionCode);

        var title = Optional.ofNullable(aboutScreenConfigurationMap.get(FormConstant.PORTAL_ABOUT_SCREEN_CONFIGURATION_PAGE_TITLE))
            .map(ResponseUserAnswerForm::getAdditionalAnswer)
            .orElse(StringUtils.EMPTY);
        var subTitle = Optional.ofNullable(aboutScreenConfigurationMap.get(FormConstant.PORTAL_ABOUT_SCREEN_CONFIGURATION_PAGE_SUB_TITLE))
            .map(ResponseUserAnswerForm::getAdditionalAnswer)
            .orElse(StringUtils.EMPTY);
        var responseBuilder = ResponseRecommendApplyApplication.builder()
            .title(title)
            .subTitle(subTitle)
            .primaryLogo(portal.getPrimaryLogo());
        var technicalAssistanceSubmit = portalFormService.getSubmitTechnicalAssistance(portal);
        if (Objects.nonNull(technicalAssistanceSubmit)) {
            responseBuilder.status(technicalAssistanceSubmit.getStatus())
                .assignAwardHours(technicalAssistanceSubmit.getAssignAwardHours())
                .remainingAwardHours(technicalAssistanceSubmit.getRemainingAwardHours());
        }
        return responseBuilder.build();
    }

    public List<IResponseLocation> getPortalByCommunityPartnerId(UUID communityPartnerId) {
        return portalRepository.getPortalByCommunityPartnerId(communityPartnerId);
    }
}
