package com.formos.huub.service.member;

import com.formos.huub.domain.constant.FormConstant;
import com.formos.huub.domain.constant.MemberConstant;
import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.*;
import com.formos.huub.domain.request.bookingsetting.RequestBookingSetting;
import com.formos.huub.domain.request.member.*;
import com.formos.huub.domain.response.member.*;
import com.formos.huub.domain.response.technicaladvisor.IResponseTechnicalAdvisorSetting;
import com.formos.huub.domain.response.usersetting.IResponseUserSetting;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.helper.member.MemberHelper;
import com.formos.huub.helper.portal.PortalHelper;
import com.formos.huub.mapper.member.MemberMapper;
import com.formos.huub.mapper.technicaladvisor.TechnicalAdvisorMapper;
import com.formos.huub.repository.*;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.bookingsetting.BookingSettingService;
import com.formos.huub.service.invite.InviteService;
import com.formos.huub.service.pushnotification.PushNotificationService;
import com.formos.huub.service.user.UserHelper;
import com.formos.huub.service.useranswerform.UserFormService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.security.RandomUtil;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MemberService extends BaseService {

    private static final String MEMBER_TECHNICAL_ADVISOR = "MEMBER-TECHNICAL-ADVISOR";

    private static final String MEMBER_BUSINESS_OWNER = "MEMBER-BUSINESS-OWNER";

    private static final String MEMBER_COMMUNITY_PARTNER = "MEMBER_COMMUNITY_PARTNER";

    MemberRepository memberRepository;

    PortalHostRepository portalHostRepository;

    UserRepository userRepository;

    MemberMapper memberMapper;

    AuthorityRepository authorityRepository;

    TechnicalAdvisorRepository technicalAdvisorRepository;

    CacheManager cacheManager;

    PortalRepository portalRepository;

    CommunityPartnerRepository communityPartnerRepository;

    TechnicalAdvisorMapper technicalAdvisorMapper;

    InviteService inviteService;

    UserHelper userHelper;

    FollowRepository followRepository;

    BookingSettingService bookingSettingService;

    PortalHelper portalHelper;

    BusinessOwnerRepository businessOwnerRepository;

    UserFormService userFormService;

    MemberHelper memberHelper;

    UserSettingRepository userSettingRepository;

    BlockedUserRepository blockedUserRepository;

    ActivityLogRepository activityLogRepository;

    PushNotificationService pushNotificationService;

    AppointmentRepository appointmentRepository;

    ProjectRepository projectRepository;

    TechnicalAssistanceAdvisorRepository technicalAssistanceAdvisorRepository;


    @Transactional(readOnly = true)
    public Map<String, Object> getAllMemberWithPageable(RequestSearchMember request) {
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "u.createdDate,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        if (Objects.isNull(request.getPortalId())) {
            request.setPortalId(PortalContextHolder.getPortalId());
        }
        if (ViewTypeEnum.TABLE.equals(request.getViewType())) {
            validateTableAccess();
            return searchTableView(request, pageable);
        }
        var currentUser = getCurrentUser();
        if (Objects.nonNull(currentUser)) {
            request.setCurrentUserId(currentUser.getId());
        }
        if (Objects.nonNull(request.getUserId())) {
            request.setCurrentUserId(request.getUserId());
        }
        var isAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.SYSTEM_ADMINISTRATOR);
        if (!isAdmin) {
            request.setExcludeRoles(List.of(AuthoritiesConstants.SYSTEM_ADMINISTRATOR));
        }
        return PageUtils.toPage(memberRepository.searchMemberByConditions(request, pageable));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAllMemberInGroupWithPageable(RequestSearchMemberInGroup request) {
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "u.createdDate,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        var currentUser = getCurrentUser();
        if (Objects.nonNull(currentUser)) {
            request.setCurrentUserId(currentUser.getId());
        }
        request.setSearchKeyword(StringUtils.makeStringWithContain(request.getSearchKeyword()));
        return PageUtils.toPage(memberRepository.searchGroupMemberByConditions(request, pageable));
    }

    public Map<String, Object> getAllMemberConnectionByUser(UUID userId, RequestSearchMemberConnection request) {
        var requestSearch = RequestSearchMember.builder()
            .userId(userId)
            .currentUserId(userId)
            .viewType(ViewTypeEnum.CARD)
            .followerStatus(FollowStatusEnum.CONNECTED)
            .build();
        BeanUtils.copyProperties(request, requestSearch);
        return getAllMemberWithPageable(requestSearch);
    }

    public Map<String, Object> searchTableView(RequestSearchMember request, Pageable pageable) {
        HashMap<String, String> sortMap = new HashMap<>();
        sortMap.put("normalizedFullName", "u.normalizedFullName");
        sortMap.put("email", "u.email");
        sortMap.put("role", "au.name");
        sortMap.put("status", "u.status");
        request.setSearchConditions(ObjectUtils.convertSortParams(request.getSearchConditions(), sortMap));
        return PageUtils.toPage(memberRepository.getAllMemberWithPageable(request, request.getPortalId(), pageable));
    }

    public List<ResponsePortalByRole> getPortalByUser(UUID userId, boolean isAll) {
        var user = getMember(userId);
        var authority = getAuthorityUser(user);
        UUID portalId = isAll ? null : PortalContextHolder.getPortalId();
        List<Portal> portals = memberHelper
            .getPortalsByAuthority(userId, authority, user)
            .stream()
            .filter(ele -> Objects.isNull(portalId) || ele.getId().equals(portalId))
            .toList();
        return memberHelper.convertToResponse(portals);
    }

    public void followUser(RequestFollowerUser request) {
        var followed = getMember(request.getUserId());
        var currentUser = getCurrentUser();
        var existFollow = followRepository.existsByFollowedIdAndFollowerIdAndStatus(
            followed.getId(),
            currentUser.getId(),
            request.getStatus()
        );
        if (existFollow) {
            return;
        }
        var follow = followRepository
            .findByFollowedIdAndFollowerId(followed.getId(), currentUser.getId())
            .orElse(Follow.builder().followed(followed).follower(currentUser).build());
        follow.setStatus(request.getStatus());
        followRepository.save(follow);
        if (FollowStatusEnum.CONNECTED.equals(request.getStatus())) {
            pushNotificationService.sendNewFollowerForMember(followed);
        }
    }

    public void createMember(RequestMember request) {
        validateMember(request.getMemberProfile());

        User loginUser = getCurrentLoginUser();
        RequestMemberProfile memberProfile = request.getMemberProfile();

        Set<Authority> authorities = getAuthoritiesForRole(memberProfile.getRole());
        String resetKey = generateResetKeyForMember(request, loginUser);

        User user = buildNewUser(request, authorities, loginUser, resetKey);
        user = userRepository.saveAndFlush(user);
        insertUserSettings(user.getId());

        createOrUpdateMemberByRole(user, memberProfile.getRole(), request, true);
    }

    /**
     * Insert User Settings
     *
     * @param userId UUID
     */
    private void insertUserSettings(UUID userId) {
        userRepository.insertUserSettings(userId);
    }

    private void validateMember(RequestMemberProfile request) {
        var isExistEmail = userRepository.existsByLoginIgnoreCaseIncludeDeleted(request.getEmail());
        if (isExistEmail) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "Email"));
        }
        var isExistUsername = userRepository.existsByUsernameIgnoreCase(request.getUsername());
        if (isExistUsername) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "Username"));
        }
        if (AuthoritiesConstants.PORTAL_HOST.equalsIgnoreCase(request.getRole()) && Objects.isNull(request.getPortalId())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0016, "Portal Name"));
        }

        validateNavigatorOrPrimaryContactMember(request);
    }

    private User getCurrentLoginUser() {
        return SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneWithAuthoritiesByLogin)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "User Login")));
    }

    private Set<Authority> getAuthoritiesForRole(String role) {
        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(role).ifPresent(authorities::add);
        return authorities;
    }

    private String generateResetKeyForMember(RequestMember request, User loginUser) {
        String resetKey = RandomUtil.generateResetKey();
        request.setResetKey(resetKey);
        request.setLoginUser(loginUser);
        return resetKey;
    }

    private User buildNewUser(RequestMember memberProfile, Set<Authority> authorities, User loginUser, String resetKey) {
        User user = memberMapper.toEntity(memberProfile.getMemberProfile());
        boolean isActivated = UserStatusEnum.ACTIVE.getValue().equals(memberProfile.getMemberProfile().getStatus());

        user.setLogin(user.getEmail());
        user.setActivated(isActivated);
        user.setAuthorities(authorities);
        user.setOrganization(loginUser.getOrganization());
        user.setResetKey(resetKey);
        user.setResetDate(Instant.now());
        user.setPassword(userHelper.genRandomPassword());
        var bookingSettingRequest = memberProfile.getBookingSetting();
        if (Objects.nonNull(bookingSettingRequest)) {
            user.setAllowBooking(bookingSettingRequest.isAllowBooking());
        }
        userRepository.save(user);
        return user;
    }

    private void createOrUpdateMemberByRole(User user, String authority, RequestMember request, boolean isCreate) {
        switch (authority) {
            case AuthoritiesConstants.SYSTEM_ADMINISTRATOR:
                break;
            case AuthoritiesConstants.PORTAL_HOST:
                savePortalHost(user, request, isCreate);
                break;
            case AuthoritiesConstants.COMMUNITY_PARTNER:
                saveCommunityPartner(user, request, isCreate);
                break;
            case AuthoritiesConstants.BUSINESS_OWNER:
                saveBusinessOwner(user, request, isCreate);
                break;
            case AuthoritiesConstants.TECHNICAL_ADVISOR:
                saveTechnicalAdvisor(user, request, isCreate);
                break;
            default:
                throw new IllegalArgumentException("Invalid role value");
        }
    }

    private void saveCommunityPartner(User user, RequestMember request, boolean isCreate) {
        RequestMemberProfile memberProfile = request.getMemberProfile();
        validateCommunityPartnerMember(memberProfile);
        RequestBookingSetting bookingSetting = request.getBookingSetting();
        UUID communityPartnerId = memberProfile.getCommunityPartnerId();
        AtomicReference<Portal> portal = new AtomicReference<>();
        if (Objects.nonNull(request.getPortalId())) {
            portalRepository.findById(request.getPortalId()).ifPresent(portal::set);
        }

        CommunityPartner communityPartner = getCommunityPartner(communityPartnerId);
        user.setCommunityPartner(communityPartner);
        if (Boolean.TRUE.equals(memberProfile.getIsNavigator())) {
            userRepository.updateUserNavigator(communityPartnerId, user.getId());
        }
        if (Boolean.TRUE.equals(memberProfile.getIsPrimary())) {
            userRepository.updateUserPrimary(communityPartnerId, user.getId());
        }

        // Save booking setting
        if (Objects.nonNull(bookingSetting)) {
            bookingSetting.setUserId(user.getId());
            if (bookingSetting.isAllowBooking()) {
                saveUserBookingSetting(bookingSetting);
            }
        }

        if (isCreate) {
            sendInvitationCommunityPartnerEmail(user, request.getResetKey(), communityPartner);
        }
    }

    private void validateCommunityPartnerMember(RequestMemberProfile memberProfile) {
        if (Objects.isNull(memberProfile.getCommunityPartnerId())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0016, "Community Partner Organization"));
        }
    }

    private void savePortalHost(User user, RequestMember request, boolean isCreate) {
        RequestMemberProfile memberProfile = request.getMemberProfile();
        RequestBookingSetting bookingSetting = request.getBookingSetting();
        UUID portalId = Objects.nonNull(memberProfile.getPortalId())
            ? memberProfile.getPortalId()
            : PortalContextHolder.getContext().getPortalId();
        Portal portal = portalRepository
            .findById(portalId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Portal")));

        PortalHost portalHost = findPortalHostByUser(user);
        portalHost.setUserId(user.getId());
        portalHost.setFirstName(memberProfile.getFirstName());
        portalHost.setLastName(memberProfile.getLastName());
        portalHost.setEmail(memberProfile.getEmail());
        portalHost.setStatus(PortalHostStatusEnum.ACTIVE);
        portalHost.setPortal(portal);
        portalHostRepository.save(portalHost);

        // Save booking setting
        if (Objects.nonNull(bookingSetting)) {
            bookingSetting.setUserId(user.getId());
            if (bookingSetting.isAllowBooking()) {
                saveUserBookingSetting(bookingSetting);
            }
        }

        var portalName = portal.getPlatformName();
        if (isCreate) {
            sendInvitationPortalHostEmail(user, request.getResetKey(), portalName, portal);
        }
    }

    private PortalHost findPortalHostByUser(User user) {
        var portalHostOpt = portalHostRepository.findByUserId(user.getId());
        return portalHostOpt.orElseGet(() -> portalHostRepository.findByEmailIgnoreCase(user.getEmail()).orElse(new PortalHost()));
    }

    /**
     * Save user BookingSetting
     *
     * @param bookingSetting RequestBookingSetting
     */
    private void saveUserBookingSetting(RequestBookingSetting bookingSetting) {
        try {
            bookingSettingService.updateBookingSetting(bookingSetting);
        } catch (Exception e) {
            throw new BadRequestException(MessageHelper.getMessage(e.getLocalizedMessage()));
        }
    }

    private void saveTechnicalAdvisor(User user, RequestMember request, boolean isCreate) {
        RequestTechnicalAdvisor requestTechnicalAdvisor = request.getTechnicalAdvisor();
        RequestTechnicalAdvisorSetting technicalAdvisorSetting = requestTechnicalAdvisor.getTechnicalAdvisorSetting();
        Set<Portal> portals = getPortals(technicalAdvisorSetting.getPortalIds());
        CommunityPartner communityPartner = getCommunityPartner(UUID.fromString(technicalAdvisorSetting.getCommunityPartnerId()));
        User loginUser = request.getLoginUser();
        String resetKey = request.getResetKey();

        TechnicalAdvisor technicalAdvisor;
        Optional<TechnicalAdvisor> technicalAdvisorOpt = technicalAdvisorRepository.findIncludeDeleteByUserId(user.getId());
        if (technicalAdvisorOpt.isPresent()) {
            technicalAdvisor = technicalAdvisorOpt.get();
            technicalAdvisor.setDelete(false);
            technicalAdvisor.setCommunityPartner(communityPartner);
            technicalAdvisor.setPortals(portals);
        } else {
            technicalAdvisor = TechnicalAdvisor.builder().user(user).communityPartner(communityPartner).portals(portals).build();
        }
        technicalAdvisorRepository.save(technicalAdvisor);
        if (isCreate) {
            sendInvitationTechnicalAdvisorEmail(loginUser, user, resetKey, communityPartner);
        }
    }

    private void saveBusinessOwner(User user, RequestMember request, boolean isCreate) {
        RequestMemberProfile memberProfile = request.getMemberProfile();
        UUID portalId = Objects.nonNull(memberProfile.getPortalId()) ? memberProfile.getPortalId() : PortalContextHolder.getPortalId();
        Portal portal = portalHelper.getPortal(portalId);

        BusinessOwner businessOwner = businessOwnerRepository.findByUserEmailIgnoreCase(user.getEmail()).orElse(new BusinessOwner());
        businessOwner.setUser(user);
        businessOwner.setPortal(portal);
        businessOwnerRepository.save(businessOwner);

        userFormService.fillFormBusinessOwner(user.getId(), request.getBusinessOwner(), FormConstant.BUSINESS_OWNER_FORM);

        if (isCreate) {
            sendInvitationBusinessOwnerEmail(user, request.getResetKey(), portal);
        }
    }

    private void sendInvitationTechnicalAdvisorEmail(User loginUser, User user, String resetKey, CommunityPartner communityPartner) {
        String guestEmail = user.getEmail();
        String guestName = String.join(
            " ",
            Optional.ofNullable(user.getFirstName()).orElse(""),
            Optional.ofNullable(user.getLastName()).orElse("")
        ).trim();

        String senderName = String.join(
            " ",
            Optional.ofNullable(loginUser.getFirstName()).orElse(""),
            Optional.ofNullable(loginUser.getLastName()).orElse("")
        ).trim();

        List<Portal> technicalAdvisorPortals = technicalAdvisorRepository.findAllPortalByUserIdOfTa(user.getId());

        inviteService.sendInvitationEmail(
            senderName,
            guestEmail,
            guestName,
            resetKey,
            MEMBER_TECHNICAL_ADVISOR,
            loginUser.getOrganization(),
            technicalAdvisorPortals
        );
    }

    private void sendInvitationPortalHostEmail(User user, String resetKey, String portalName, Portal portal) {
        String guestEmail = user.getEmail();
        String guestName = String.join(" ", user.getFirstName(), user.getLastName());
        inviteService.sendInvitationPortalHost(guestEmail, guestName, resetKey, portalName, true, true, portal);
    }

    private void sendInvitationCommunityPartnerEmail(User user, String resetKey, CommunityPartner communityPartner) {
        String guestEmail = user.getEmail();
        Set<Portal> portals = communityPartner.getPortals();
        inviteService.sendInvitationCommunityPartner(
            guestEmail,
            user.getNormalizedFullName(),
            resetKey,
            portals,
            MEMBER_COMMUNITY_PARTNER,
            false,
            communityPartner.getName()
        );
    }

    private void sendInvitationBusinessOwnerEmail(User user, String resetKey, Portal portal) {
        String guestEmail = user.getEmail();
        String guestName = String.join(" ", user.getFirstName(), user.getLastName());
        inviteService.sendInvitationBusinessOwner(guestEmail, guestName, resetKey, MEMBER_BUSINESS_OWNER, portal);
    }

    private Set<Portal> getPortals(List<UUID> portalIds) {
        if (ObjectUtils.isEmpty(portalIds)) {
            return null;
        }
        return new HashSet<>(portalRepository.findAllById(portalIds));
    }

    private CommunityPartner getCommunityPartner(UUID communityPartnerId) {
        return communityPartnerRepository
            .findById(communityPartnerId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Community Partner")));
    }

    public void updateMember(UUID memberId, RequestMember request) {
        User user = getMember(memberId);
        RequestMemberProfile memberProfile = request.getMemberProfile();
        RequestBookingSetting bookingSetting = request.getBookingSetting();
        String role = memberProfile.getRole();
        validateUpdateMember(user, memberProfile);
        String originalRole = user
            .getAuthorities()
            .stream()
            .findFirst()
            .map(Authority::getName)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Role")));
        memberMapper.partialEntity(user, request.getMemberProfile());
        user.makeNormalizedFullName();
        if (!role.equals(originalRole)) {
            Set<Authority> authorities = new HashSet<>();
            authorityRepository.findById(role).ifPresent(authorities::add);
            user.setAuthorities(authorities);
            deleteMemberByRole(user, originalRole);
        }
        user.setActivated(memberProfile.getStatus().equals(UserStatusEnum.ACTIVE.getValue()));
        user.setLogin(memberProfile.getEmail());
        user.setAllowBooking(Objects.nonNull(bookingSetting) && bookingSetting.isAllowBooking());
        userRepository.save(user);
        insertUserSettings(memberId);

        createOrUpdateMemberByRole(user, role, request, false);

        clearUserCaches(user);
    }

    private void validateUpdateMember(User user, RequestMemberProfile memberProfile) {
        var existEmail = userRepository.existsByEmailIgnoreCase(memberProfile.getEmail());
        if (!user.getEmail().equalsIgnoreCase(memberProfile.getEmail()) && existEmail) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "Email"));
        }

        var existUsername = userRepository.existsByUsernameIgnoreCase(memberProfile.getUsername());
        if (!memberProfile.getUsername().equalsIgnoreCase(user.getUsername()) && existUsername) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "Username"));
        }
        if (AuthoritiesConstants.PORTAL_HOST.equalsIgnoreCase(memberProfile.getRole()) && Objects.isNull(memberProfile.getPortalId())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0016, "Portal Name"));
        }

        validateNavigatorOrPrimaryContactMember(memberProfile);
    }

    /**
     * Validate Navigator Or PrimaryContact Member
     *
     * @param member RequestMemberProfile
     */
    private void validateNavigatorOrPrimaryContactMember(RequestMemberProfile member) {
        if (!AuthoritiesConstants.COMMUNITY_PARTNER.equalsIgnoreCase(member.getRole())) {
            return;
        }
        if ((Boolean.TRUE.equals(member.getIsPrimary()) || Boolean.TRUE.equals(member.getIsNavigator()))
            && !UserStatusEnum.ACTIVE.equals(UserStatusEnum.valueOf(member.getStatus()))) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0089));
        }
    }

    public void deleteMember(UUID memberId) {

        User user = getMember(memberId);
        String originalRole = user
            .getAuthorities()
            .stream()
            .findFirst()
            .map(Authority::getName)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Role")));

        deleteMemberByRole(user, originalRole);
        if (UserStatusEnum.ACTIVE.equals(user.getStatus())){
            user.setStatus(UserStatusEnum.INACTIVE);
        }
        memberRepository.save(user);
        memberRepository.deleteMemberById(memberId);
    }

    private void deleteMemberByRole(User user, String authority) {
        switch (authority) {
            case AuthoritiesConstants.SYSTEM_ADMINISTRATOR:
                break;
            case AuthoritiesConstants.PORTAL_HOST:
                boolean isCurrentlyInUse = portalHostRepository.existPrimaryPortalHostByMemberId(user.getId());
                if (isCurrentlyInUse) {
                    throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0036, "Portal Host"));
                }
                PortalHost portalHost = portalHostRepository
                    .findByUserId(user.getId())
                    .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Portal Host")));
                portalHostRepository.delete(portalHost);
                break;
            case AuthoritiesConstants.COMMUNITY_PARTNER:
                break;
            case AuthoritiesConstants.BUSINESS_OWNER:
                break;
            case AuthoritiesConstants.TECHNICAL_ADVISOR:
                TechnicalAdvisor technicalAdvisor = user.getTechnicalAdvisor();
                var isProjectUse = projectRepository.existsUseByTechnicalAdvisor(technicalAdvisor.getId());
                var isAppointmentUse = appointmentRepository.existsUseByTechnicalAdvisor(technicalAdvisor.getId());
                if (isProjectUse || isAppointmentUse) {
                    throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0036, "Technical Advisor"));
                }
                technicalAdvisorRepository.deleteTechnicalAdvisorById(technicalAdvisor.getId());
                break;
            default:
                throw new IllegalArgumentException("Invalid role value");
        }
    }

    public ResponseMemberDetail getMemberById(UUID memberId) {
        User user = getMember(memberId);
        String role = getMemberRole(user);
        ResponseMemberDetail responseMemberDetail = new ResponseMemberDetail();
        responseMemberDetail.setMemberProfile(memberMapper.toResponse(user));
        responseMemberDetail.getMemberProfile().setRole(role);

        setMemberResponseByRole(user, role, responseMemberDetail);

        return responseMemberDetail;
    }

    public ResponsePublicDetailProfile getPublicDetailProfile(UUID userId) {
        var user = getMember(userId);

        var response = memberMapper.toResponsePublic(user);
        response.setRole(getMemberRole(user));

        // Set portal names
        String portalNames = getPortalByUser(userId, false)
            .stream()
            .map(ResponsePortalByRole::getPlatformName)
            .sorted(String::compareTo)
            .collect(Collectors.joining(","));
        response.setPortalName(portalNames);

        // Add business information
        var currentUser = getCurrentUser();
        if (Objects.nonNull(currentUser)) {
            response.setIsBlocked(isBlockUser(currentUser.getId(), userId));
            response.setFollowStatus(getFollowStatus(currentUser.getId(), userId));
        }
        boolean isCurrentUser = Objects.nonNull(currentUser) && currentUser.getId().equals(userId);
        populateBusinessInfo(userId, response, isCurrentUser);
        response.setLastActivityDate(getLastActivityLog(user.getLogin()));
        return response;
    }

    private Boolean isBlockUser(UUID blocker, UUID blocked) {
        return blockedUserRepository.findByBlockerIdAndBlockedId(blocker, blocked).isPresent();
    }

    private Instant getLastActivityLog(String login) {
        return activityLogRepository.getLastActivityDate(login);
    }

    private FollowStatusEnum getFollowStatus(UUID follower, UUID followed) {
        return followRepository
            .findByFollowedIdAndFollowerId(followed, follower)
            .map(Follow::getStatus)
            .orElse(FollowStatusEnum.DISCONNECTED);
    }

    private void populateBusinessInfo(UUID userId, ResponsePublicDetailProfile response, boolean isCurrentUser) {
        // Fetch user privacy settings and map them by setting key
        var privacySettings = userSettingRepository.findAllByUserIdAndCategory(userId, SettingCategoryEnum.PRIVACY);
        var privacySettingMap = ObjectUtils.convertToMap(privacySettings, IResponseUserSetting::getSettingKey);

        // Fetch answers for business-related questions and initialize result map
        var answerBusinessInfoMap = userFormService.getAnswerUserByQuestionCode(userId, MemberConstant.BUSINESS_INFO_MAP);

        // Process privacy and business info settings
        var businessInfoMap = new HashMap<String, String>();
        MemberConstant.PRIVACY_SETTING_BUSINESS_MAP.forEach((key, questionCode) -> {
            String settingValue = getSettingValue(privacySettingMap, key);
            boolean isPublic = "public".equals(settingValue);

            if (isPublic || isCurrentUser) {
                if (!MemberConstant.FIELD.equals(questionCode)) {
                    businessInfoMap.put(questionCode, answerBusinessInfoMap.getOrDefault(questionCode, ""));
                }
            } else {
                updatePrivateFields(response, key);
            }
        });

        response.setBusinessInfo(businessInfoMap);
    }

    private String getSettingValue(Map<String, IResponseUserSetting> settingMap, SettingKeyCodeEnum key) {
        IResponseUserSetting setting = settingMap.get(key.getValue());
        return setting != null ? setting.getSettingValue() : null;
    }

    private void updatePrivateFields(ResponsePublicDetailProfile response, SettingKeyCodeEnum key) {
        switch (key) {
            case HUUB_PORTAL -> response.setPortalName(null);
            case LAST_NAME -> response.setLastName(null);
            case BUSINESS_WEBSITE -> response.setPersonalWebsite(null);
        }
    }

    private String getMemberRole(User user) {
        return user
            .getAuthorities()
            .stream()
            .findFirst()
            .map(Authority::getName)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Role")));
    }

    private void setMemberResponseByRole(User user, String authority, ResponseMemberDetail responseMemberDetail) {
        switch (authority) {
            case AuthoritiesConstants.SYSTEM_ADMINISTRATOR:
                break;
            case AuthoritiesConstants.PORTAL_HOST:
                setPortalHostDetails(user, responseMemberDetail);
                break;
            case AuthoritiesConstants.COMMUNITY_PARTNER:
                setCommunityPartnerDetails(user, responseMemberDetail);
                break;
            case AuthoritiesConstants.BUSINESS_OWNER:
                setBusinessOwnerDetails(user, responseMemberDetail);
                break;
            case AuthoritiesConstants.TECHNICAL_ADVISOR:
                TechnicalAdvisor technicalAdvisor = user.getTechnicalAdvisor();
                IResponseTechnicalAdvisorSetting technicalAdvisorSetting = technicalAdvisorRepository.getTechnicalAdvisorSetting(
                    technicalAdvisor.getId()
                );
                ResponseTechnicalAdvisorSetting responseSettings = technicalAdvisorMapper.toResponseFromInterface(technicalAdvisorSetting);
                ResponseTechnicalAdvisor responseTechnicalAdvisor = new ResponseTechnicalAdvisor();
                responseTechnicalAdvisor.setId(technicalAdvisor.getId());
                responseTechnicalAdvisor.setTechnicalAdvisorSetting(responseSettings);
                responseMemberDetail.setTechnicalAdvisor(responseTechnicalAdvisor);
                break;
            default:
                throw new IllegalArgumentException("Invalid role value");
        }
    }

    /**
     * Set PortalHost details for getMemberById
     *
     * @param user                 User
     * @param responseMemberDetail ResponseMemberDetail
     */
    private void setPortalHostDetails(User user, ResponseMemberDetail responseMemberDetail) {
        PortalHost portalHost = portalHostRepository
            .findByUserId(user.getId())
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Portal Host")));

        responseMemberDetail.setIsPortalHostPrimary(portalHost.getIsPrimary());
        Optional.ofNullable(portalHost.getPortal()).ifPresent(
            portal -> responseMemberDetail.getMemberProfile().setPortalId(portal.getId())
        );

        if (Objects.nonNull(user.getAllowBooking()) && user.getAllowBooking()) {
            responseMemberDetail.setBookingSetting(bookingSettingService.getSettingOfUser(user.getId()));
        }
    }

    private void setCommunityPartnerDetails(User user, ResponseMemberDetail responseMemberDetail) {
        CommunityPartner communityPartner = user.getCommunityPartner();
        if (Objects.nonNull(communityPartner)) {
            responseMemberDetail.getMemberProfile().setCommunityPartnerId(communityPartner.getId());
        }
        if (Objects.nonNull(user.getAllowBooking()) && user.getAllowBooking()) {
            responseMemberDetail.setBookingSetting(bookingSettingService.getSettingOfUser(user.getId()));
        }
    }

    private void setBusinessOwnerDetails(User user, ResponseMemberDetail responseMemberDetail) {
        BusinessOwner businessOwner = businessOwnerRepository.findByUserId(user.getId()).orElse(null);
        if (Objects.nonNull(businessOwner)) {
            Optional.ofNullable(businessOwner.getPortal()).ifPresent(
                portal -> responseMemberDetail.getMemberProfile().setPortalId(portal.getId())
            );
        }
        UUID portalId = responseMemberDetail.getMemberProfile().getPortalId();
        responseMemberDetail.setBusinessOwner(userFormService.getAllBusinessOwnerQuestionByPortal(portalId, FormConstant.BUSINESS_OWNER_FORM));
    }

    private void validateTableAccess() {
        if (!SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.SYSTEM_ADMINISTRATOR, AuthoritiesConstants.PORTAL_HOST)) {
            throw new AccessDeniedException("Role Member");
        }
    }

    private User getMember(UUID memberId) {
        return userRepository
            .findById(memberId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Member")));
    }

    private User getCurrentUser() {
        var userLogin = SecurityUtils.getCurrentUserLogin();
        return userLogin.flatMap(userRepository::findOneByLogin).orElse(null);
    }

    private String getAuthorityUser(User user) {
        return user
            .getAuthorities()
            .stream()
            .findFirst()
            .map(Authority::getName)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Role")));
    }

    private void clearUserCaches(User user) {
        Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)).evict(user.getLogin());
        if (user.getEmail() != null) {
            Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE)).evict(user.getEmail());
        }
    }
}
