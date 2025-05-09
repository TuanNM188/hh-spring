package com.formos.huub.service.invite;

import com.formos.huub.config.ApplicationProperties;
import com.formos.huub.config.Constants;
import com.formos.huub.domain.constant.BusinessConstant;
import com.formos.huub.domain.constant.EmailTemplatePathsConstants;
import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.StatusEnum;
import com.formos.huub.domain.enums.UserStatusEnum;
import com.formos.huub.domain.request.invitemember.RequestEmailCommunityPartner;
import com.formos.huub.domain.request.invitemember.RequestEmailTechnicalAdvisor;
import com.formos.huub.domain.request.invitemember.RequestInviteMember;
import com.formos.huub.domain.response.communitypartner.ResponseExistsCommunityPartnerStaff;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.constant.AppConstants;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.service.mail.IMailService;
import com.formos.huub.framework.service.storage.model.CloudProperties;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.helper.file.FileHelper;
import com.formos.huub.repository.*;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.user.UserHelper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.security.RandomUtil;

import java.text.Normalizer;
import javax.sound.sampled.Port;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.formos.huub.framework.constant.AppConstants.*;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InviteService extends BaseService {

    UserRepository userRepository;
    IMailService mailService;
    ApplicationProperties applicationProperties;
    CommunityPartnerRepository communityPartnerRepository;
    AuthorityRepository authorityRepository;
    PortalRepository portalRepository;
    TechnicalAdvisorRepository technicalAdvisorRepository;
    CloudProperties config;
    UserHelper userHelper;
    FileHelper fileHelper;

    private static final String TOKEN = "token";
    private static final String ACTIVE_TYPE = "activeType";
    private static final String INVITE_SENDER_NAME = "inviteSenderName";
    private static final String INVITE_SENDER_ORGANIZATION_NAME = "inviteSenderOrganizationName";
    private static final String SUPPORT_EMAIL = "supportEmail";
    private static final String LIVE_CHAT = "liveChat";
    private static final String TYPE = "type";
    private static final String PORTALS = "portals";
    private static final String COMMUNITY_PARTNER_NAME = "communityPartnerName";
    private static final String TECHNICAL_ADVISOR = "TECHNICAL-ADVISOR";
    private static final String PATTERN_USERNAME = "%s%s"; // {firstName}{lastname}
    private static final String MEMBER_COMMUNITY_PARTNER = "MEMBER_COMMUNITY_PARTNER";

    // extension request project
    private static final String PROJECT_NAME = "projectName";
    private static final String NAVIGATOR_FIRST_NAME = "navigatorFirstName";
    private static final String NAVIGATOR_FULL_NAME = "navigatorFullName";
    private static final String NAVIGATOR_EMAIL = "navigatorEmail";
    private static final String USER_NAME = "userName";
    private static final String NEW_COMPLETION_DATE = "newCompletionDate";
    private static final String ADVISOR_FULL_NAME = "advisorFullName";
    private static final String ADVISOR_FIRST_NAME = "advisorFirstName";
    private static final String ADVISOR_EMAIL = "advisorEmail";
    private static final String BUSINESS_OWNER_FIRST_NAME = "businessOwnerFirstName";
    private static final String BUSINESS_OWNER_FULL_NAME = "businessOwnerFullName";
    private static final String THREE_DAYS_LATER = "threeDaysLater";
    private static final String LINK_TO_PROJECT_DETAIL = "linkToProjectDetail";

    private static final String SUPPORT_LIVE_CHAT = "supportLiveChat";



    public UUID inviteTechnicalAdvisor(RequestInviteMember request) {
        String guestEmail = request.getEmail();
        validateExistingUser(guestEmail);

        User currentUser = getCurrentLoginUser();
        prepareRequestForInvitation(request, currentUser);

        CommunityPartner communityPartner = getCommunityPartner(request.getCommunityPartnerId());
        TechnicalAdvisor technicalAdvisor = saveTechnicalAdvisorInvite(request, communityPartner);

        // send email to Technical advisor invited
        sendInvitationEmailToTechnicalAdvisor(request, currentUser, guestEmail, technicalAdvisor);

        return technicalAdvisor.getId();
    }

    /**
     * Sends an invitation email to a technical advisor.
     *
     * @param senderName            The user who is sending the invitation.
     * @param technicalAdvisorEmail The email address of the technical advisor.
     * @param technicalAdvisorName  The name of the technical advisor.
     * @param inviteToken           The unique token generated for the invitation.
     */
    public void sendInvitationEmail(String senderName, String technicalAdvisorEmail, String technicalAdvisorName,
                                    String inviteToken, String type, String organization, List<Portal> portals) {
        organization = Objects.nonNull(organization) ? organization : "HUUB";

        List<RequestEmailTechnicalAdvisor> portalList = portals.stream().map(portal -> new RequestEmailTechnicalAdvisor(
            portal.getPlatformName(),
            buildPortalUrl(portal)
        )).toList();

        String templatePath = EmailTemplatePathsConstants.INVITE_TECHNICAL_ADVISOR;
        String title = MessageHelper.getMessage("email.invitation.title", List.of(organization));
        String titleReplace = String.join("", "[", organization, "]");
        title = title.replace(titleReplace, organization);
        String supportEmail = applicationProperties.getCustomerCare().getJoinHuubSupport();
        String liveChat = applicationProperties.getCustomerCare().getJoinHuubLiveChat();
        Portal portal = portals.stream().findFirst().orElse(null);
        String portalPrimaryLogo = Optional.ofNullable(portal).map(Portal::getPrimaryLogo).orElse(null);

        HashMap<String, Object> mapContents = new HashMap<>();
        mapContents.put(TOKEN, inviteToken);
        mapContents.put(INVITE_SENDER_NAME, senderName);
        mapContents.put(INVITE_SENDER_ORGANIZATION_NAME, organization);
        mapContents.put(RECEIVER_NAME, technicalAdvisorName);
        mapContents.put(SUPPORT_EMAIL, supportEmail);
        mapContents.put(LIVE_CHAT, liveChat);
        mapContents.put(TYPE, type);
        mapContents.put(PORTALS, portalList);
        mapContents.put(LOGO_IMAGE, fileHelper.primaryPortalLogo(portalPrimaryLogo));
        mapContents.put(BASE_URL, buildPortalUrl(portal));

        mailService.sendEmailFromTemplate(technicalAdvisorEmail, AppConstants.DEFAULT_LANGUAGE, mapContents, templatePath, title);
    }

    /**
     * Sends an invitation email to a portal Host.
     *
     * @param email       The email address of the portal host.
     * @param name        The name of the portal host.
     * @param inviteToken The unique token generated for the invitation.
     */
    public void sendInvitationPortalHost(String email, String name, String inviteToken, String portalName, boolean isExist, boolean isCreateMember, Portal portal) {
        String clientAppUrl = buildPortalUrl(portal);
        String templatePath = isCreateMember ? EmailTemplatePathsConstants.INVITE_MEMBER_PORTAL_HOST :
            isExist ? EmailTemplatePathsConstants.INVITE_EXIST_PORTAL_HOST : EmailTemplatePathsConstants.INVITE_NEW_PORTAL_HOST;
        String title = MessageHelper.getMessage("email.invitation.portalHost.exist.title", List.of(portalName));
        String titleReplace = String.join("", "[", portalName, "]");
        title = title.replace(titleReplace, portalName);
        String supportEmail = applicationProperties.getCustomerCare().getJoinHuubSupport();
        String liveChat = applicationProperties.getCustomerCare().getJoinHuubLiveChat();

        HashMap<String, Object> mapContents = new HashMap<>();
        mapContents.put(TOKEN, inviteToken);
        mapContents.put(BASE_URL, clientAppUrl);
        mapContents.put(RECEIVER_NAME, name);
        mapContents.put(PORTAL_NAME, portalName);
        mapContents.put(SUPPORT_EMAIL, supportEmail);
        mapContents.put(LOGO_IMAGE, fileHelper.primaryPortalLogo(portal.getPrimaryLogo()));
        mapContents.put(LIVE_CHAT, liveChat);

        mailService.sendEmailFromTemplate(email, AppConstants.DEFAULT_LANGUAGE, mapContents, templatePath, title);
    }

    /**
     * Send invitation Business Owner
     *
     * @param email       String
     * @param name        String
     * @param inviteToken String
     */
    public void sendInvitationBusinessOwner(String email, String name, String inviteToken, String activeType, Portal portal) {
        String clientAppUrl = buildPortalUrl(portal);
        String templatePath = EmailTemplatePathsConstants.INVITE_MEMBER_BUSINESS_OWNER;
        String title = MessageHelper.getMessage("email.invitation.member.businessOwner.title", List.of(KEY_EMPTY));

        HashMap<String, Object> mapContents = new HashMap<>();
        mapContents.put(TOKEN, inviteToken);
        mapContents.put(BASE_URL, clientAppUrl);
        mapContents.put(RECEIVER_NAME, name);
        mapContents.put(ACTIVE_TYPE, activeType);
        mapContents.put(LOGO_IMAGE, fileHelper.primaryPortalLogo(portal.getPrimaryLogo()));

        mailService.sendEmailFromTemplate(email, AppConstants.DEFAULT_LANGUAGE, mapContents, templatePath, title);
    }

    /**
     * Send invitation Community Partner
     *
     * @param email       String
     * @param name        String
     * @param inviteToken String
     */
    public void sendInvitationCommunityPartner(String email, String name, String inviteToken, Set<Portal> portals, String activeType,
                                               Boolean isExist, String communityPartnerName) {
        List<RequestEmailCommunityPartner> portalList = portals.stream().map(portal -> new RequestEmailCommunityPartner(
            portal.getPlatformName(),
            buildPortalUrl(portal)
        )).toList();

        String templatePath = isExist ? EmailTemplatePathsConstants.INVITE_MEMBER_COMMUNITY_PARTNER_EXIST : EmailTemplatePathsConstants.INVITE_MEMBER_COMMUNITY_PARTNER;
        String title = MessageHelper.getMessage(MessageHelper.getMessageWithCode("email.invitation.community.partner.invite.title"), KEY_EMPTY);
        String supportEmail = applicationProperties.getCustomerCare().getJoinHuubSupport();
        Portal portal = portals.stream().findFirst().orElse(null);
        String portalPrimaryLogo = Optional.ofNullable(portal).map(Portal::getPrimaryLogo).orElse(null);

        HashMap<String, Object> mapContents = new HashMap<>();
        mapContents.put(TOKEN, inviteToken);
        mapContents.put(RECEIVER_NAME, name);
        mapContents.put(SUPPORT_EMAIL, supportEmail);
        mapContents.put(ACTIVE_TYPE, activeType);
        mapContents.put(COMMUNITY_PARTNER_NAME, communityPartnerName);
        mapContents.put(PORTALS, portalList);
        mapContents.put(LOGO_IMAGE, fileHelper.primaryPortalLogo(portalPrimaryLogo));
        mapContents.put(BASE_URL, buildPortalUrl(portal));
        mailService.sendEmailFromTemplate(email, AppConstants.DEFAULT_LANGUAGE, mapContents, templatePath, title);
    }

    /**
     * Send invitation extension request project
     *
     * @param email       String
     * @param projectName        String
     * @param requestExtensionUserName String
     * @param navigatorFirstName String
     */
    public void sendExtensionRequest(String email, String projectName, String requestExtensionUserName, String navigatorFirstName, Project project) {

        String templatePath = EmailTemplatePathsConstants.EXTENSION_REQUEST_PROJECT;
        String title = MessageHelper.getMessage(MessageHelper.getMessageWithCode("email.invitation.project.extension.request.title"), projectName);
        String portalUrl = buildPortalUrl(project.getPortal());
        String linkToProjectDetail = portalUrl + "/ta-managements/project-management?projectId=" + project.getId() + "&featureCode=EXTENSION_REQUEST";
        HashMap<String, Object> mapContents = new HashMap<>();
        mapContents.put(PROJECT_NAME, projectName);
        mapContents.put(USER_NAME, requestExtensionUserName);
        mapContents.put(NAVIGATOR_FIRST_NAME, navigatorFirstName);
        mapContents.put(LINK_TO_PROJECT_DETAIL, linkToProjectDetail);
        mapContents.put(LOGO_IMAGE, fileHelper.primaryPortalLogo(project.getPortal().getPrimaryLogo()));
        mapContents.put(BASE_URL, portalUrl);
        mailService.sendEmailFromTemplate(email, AppConstants.DEFAULT_LANGUAGE, mapContents, templatePath, title);
    }

    /**
     * Send invitation extension request project
     *
     * @param email       String
     * @param businessOwnerFullName String
     * @param advisorFullName String
     */
    public void sendNewProject(String email, String businessOwnerFullName, String navigatorFirstName, String advisorFullName, Project project) {

        String templatePath = EmailTemplatePathsConstants.NEW_PROJECT;
        String title = MessageHelper.getMessage(MessageHelper.getMessageWithCode("email.invitation.project.title"), businessOwnerFullName);
        String portalUrl = buildPortalUrl(project.getPortal());
        String linkToProjectDetail = portalUrl + "/ta-managements/project-management?projectId=" + project.getId();
        HashMap<String, Object> mapContents = new HashMap<>();
        mapContents.put(BUSINESS_OWNER_FULL_NAME, businessOwnerFullName);
        mapContents.put(NAVIGATOR_FIRST_NAME, navigatorFirstName);
        mapContents.put(ADVISOR_FULL_NAME, advisorFullName);
        mapContents.put(LINK_TO_PROJECT_DETAIL, linkToProjectDetail);
        mapContents.put(LOGO_IMAGE, fileHelper.primaryPortalLogo(project.getPortal().getPrimaryLogo()));
        mapContents.put(BASE_URL, portalUrl);
        mailService.sendEmailFromTemplate(email, AppConstants.DEFAULT_LANGUAGE, mapContents, templatePath, title);
    }

    /**
     * Send invitation extension request project
     *
     * @param email       String
     * @param projectName        String
     * @param username String
     * @param newCompletionDate String
     */
    public void sendApproveExtensionRequest(String email, String projectName, String username, String newCompletionDate, Project project, boolean isBusinessOwner) {

        String templatePath = EmailTemplatePathsConstants.APPROVE_EXTENSION_REQUEST_PROJECT;
        String title = MessageHelper.getMessage(MessageHelper.getMessageWithCode("email.invitation.project.extension.approved.title"), projectName);
        String portalUrl = buildPortalUrl(project.getPortal());
        String linkToProjectDetail = isBusinessOwner
            ? portalUrl + "/manage-1-1-support?projectId=" + project.getId()
            : portalUrl + "/ta-managements/project-management?projectId=" + project.getId();
        HashMap<String, Object> mapContents = new HashMap<>();
        mapContents.put(PROJECT_NAME, projectName);
        mapContents.put(USER_NAME, username);
        mapContents.put(NEW_COMPLETION_DATE, newCompletionDate);
        mapContents.put(LINK_TO_PROJECT_DETAIL, linkToProjectDetail);
        mapContents.put(BASE_URL, portalUrl);
        mapContents.put(LOGO_IMAGE, fileHelper.primaryPortalLogo(project.getPortal().getPrimaryLogo()));
        mailService.sendEmailFromTemplate(email, AppConstants.DEFAULT_LANGUAGE, mapContents, templatePath, title);
    }

    /**
     * Send invitation approve project
     *
     * @param email       String
     * @param advisorFullName        String
     * @param businessOwnerFirstName String
     */
    public void sendApproveProject(String email, String advisorFullName, String businessOwnerFirstName, String navigatorFullName, String navigatorEmail, Project project) {
        String templatePath = EmailTemplatePathsConstants.APPROVE_PROJECT;
        String title = MessageHelper.getMessage(MessageHelper.getMessageWithCode("email.invitation.project.approve.title"), advisorFullName);
        String supportLiveChat = applicationProperties.getCustomerCare().getJoinHuubLiveChat();
        String portalUrl = buildPortalUrl(project.getPortal());
        String linkToProjectDetail = portalUrl + "/manage-1-1-support?projectId=" + project.getId();
        HashMap<String, Object> mapContents = new HashMap<>();
        mapContents.put(ADVISOR_FULL_NAME, advisorFullName);
        mapContents.put(NAVIGATOR_FULL_NAME, navigatorFullName);
        mapContents.put(NAVIGATOR_EMAIL, navigatorEmail);
        mapContents.put(BUSINESS_OWNER_FIRST_NAME, businessOwnerFirstName);
        mapContents.put(SUPPORT_LIVE_CHAT, supportLiveChat);
        mapContents.put(BASE_URL, portalUrl);
        mapContents.put(LINK_TO_PROJECT_DETAIL, linkToProjectDetail);
        mapContents.put(LOGO_IMAGE, fileHelper.primaryPortalLogo(project.getPortal().getPrimaryLogo()));
        mailService.sendEmailFromTemplate(email, AppConstants.DEFAULT_LANGUAGE, mapContents, templatePath, title);
    }

    /**
     * Project Proposal Approved > Email Advisor
     *
     * @param email String
     * @param advisorFirstName String
     * @param businessOwnerFullName String
     * @param navigatorFullName String
     * @param navigatorEmail String
     * @param project Portal
     */
    public void sendProjectProposalApprovedForBusinessOwner(String email, String businessOwnerFullName, String advisorFirstName, String navigatorFullName, String navigatorEmail, Project project) {
        String templatePath = EmailTemplatePathsConstants.PROJECT_PROPOSAL_APPROVED;
        String title = MessageHelper.getMessage(MessageHelper.getMessageWithCode("email.invitation.project.proposal.approve.title"), businessOwnerFullName);
        String supportLiveChat = applicationProperties.getCustomerCare().getJoinHuubLiveChat();
        String portalUrl = buildPortalUrl(project.getPortal());
        String linkToProjectDetail = portalUrl + "/ta-managements/project-management?projectId=" + project.getId();

        HashMap<String, Object> mapContents = new HashMap<>();
        mapContents.put(BUSINESS_OWNER_FULL_NAME, businessOwnerFullName);
        mapContents.put(ADVISOR_FIRST_NAME, advisorFirstName);
        mapContents.put(NAVIGATOR_FULL_NAME, navigatorFullName);
        mapContents.put(NAVIGATOR_EMAIL, navigatorEmail);
        mapContents.put(SUPPORT_LIVE_CHAT, supportLiveChat);
        mapContents.put(BASE_URL, portalUrl);
        mapContents.put(LINK_TO_PROJECT_DETAIL, linkToProjectDetail);
        mapContents.put(LOGO_IMAGE, fileHelper.primaryPortalLogo(project.getPortal().getPrimaryLogo()));

        mailService.sendEmailFromTemplate(email, AppConstants.DEFAULT_LANGUAGE, mapContents, templatePath, title);
    }

    /**
     * Project Proposal Denied > Email Advisor
     *
     * @param email String
     * @param businessOwnerFullName String
     * @param advisorFirstName String
     * @param navigatorFullName String
     * @param navigatorEmail String
     * @param project Portal
     */
    public void sendProjectProposalDeniedForBusinessOwner(String email, String businessOwnerFullName, String advisorFirstName, String navigatorFullName, String navigatorEmail, Project project) {
        String templatePath = EmailTemplatePathsConstants.PROJECT_PROPOSAL_DENIED;
        String title = MessageHelper.getMessage(MessageHelper.getMessageWithCode("email.invitation.project.deny.title"), businessOwnerFullName);
        String supportLiveChat = applicationProperties.getCustomerCare().getJoinHuubLiveChat();
        String clientAppUrl = buildPortalUrl(project.getPortal());
        String linkToProjectDetail = clientAppUrl + "/ta-managements/project-management?projectId=" + project.getId();

        HashMap<String, Object> mapContents = new HashMap<>();
        mapContents.put(ADVISOR_FIRST_NAME, advisorFirstName);
        mapContents.put(NAVIGATOR_FULL_NAME, navigatorFullName);
        mapContents.put(NAVIGATOR_EMAIL, navigatorEmail);
        mapContents.put(BUSINESS_OWNER_FULL_NAME, businessOwnerFullName);
        mapContents.put(SUPPORT_LIVE_CHAT, supportLiveChat);
        mapContents.put(BASE_URL, clientAppUrl);
        mapContents.put(LOGO_IMAGE, fileHelper.primaryPortalLogo(project.getPortal().getPrimaryLogo()));
        mapContents.put(LINK_TO_PROJECT_DETAIL, linkToProjectDetail);
        mailService.sendEmailFromTemplate(email, AppConstants.DEFAULT_LANGUAGE, mapContents, templatePath, title);
    }

    /**
     * Send invitation deny project
     *
     * @param email       String
     * @param businessOwnerFullName        String
     * @param advisorFirstName String
     * @param navigatorFullName String
     * @param navigatorEmail String
     */
    public void sendDenyProject(String email, String businessOwnerFullName, String advisorFirstName, String navigatorFullName, String navigatorEmail, Portal portal) {
        String templatePath = EmailTemplatePathsConstants.DENY_PROJECT;
        String title = MessageHelper.getMessage(MessageHelper.getMessageWithCode("email.invitation.project.deny.title"), businessOwnerFullName);
        String supportLiveChat = applicationProperties.getCustomerCare().getJoinHuubLiveChat();
        String clientAppUrl = buildPortalUrl(portal);
        HashMap<String, Object> mapContents = new HashMap<>();
        mapContents.put(ADVISOR_FIRST_NAME, advisorFirstName);
        mapContents.put(NAVIGATOR_FULL_NAME, navigatorFullName);
        mapContents.put(NAVIGATOR_EMAIL, navigatorEmail);
        mapContents.put(BUSINESS_OWNER_FULL_NAME, businessOwnerFullName);
        mapContents.put(SUPPORT_LIVE_CHAT, supportLiveChat);
        mapContents.put(BASE_URL, clientAppUrl);
        mapContents.put(LOGO_IMAGE, fileHelper.primaryPortalLogo(portal.getPrimaryLogo()));
        mailService.sendEmailFromTemplate(email, AppConstants.DEFAULT_LANGUAGE, mapContents, templatePath, title);
    }


    /**
     * Send invitation extension request project
     *
     * @param email       String
     * @param projectName        String
     * @param username String
     * @param navigatorEmail String
     */
    public void sendDenyExtensionRequest(String email, String projectName, String username, String navigatorEmail, Project project, boolean isBusinessOwner) {

        String templatePath = EmailTemplatePathsConstants.DENY_EXTENSION_REQUEST_PROJECT;
        String title = MessageHelper.getMessage(MessageHelper.getMessageWithCode("email.invitation.project.extension.denied.title"), projectName);
        String portalUrl = buildPortalUrl(project.getPortal());
        String linkToProjectDetail = isBusinessOwner
            ? portalUrl + "/manage-1-1-support?projectId=" + project.getId()
            : portalUrl + "/ta-managements/project-management?projectId=" + project.getId();

        HashMap<String, Object> mapContents = new HashMap<>();
        mapContents.put(PROJECT_NAME, projectName);
        mapContents.put(USER_NAME, username);
        mapContents.put(NAVIGATOR_EMAIL, navigatorEmail);
        mapContents.put(BASE_URL, portalUrl);
        mapContents.put(LOGO_IMAGE, fileHelper.primaryPortalLogo(project.getPortal().getPrimaryLogo()));
        mapContents.put(LINK_TO_PROJECT_DETAIL, linkToProjectDetail);
        mailService.sendEmailFromTemplate(email, AppConstants.DEFAULT_LANGUAGE, mapContents, templatePath, title);
    }

    public void sendNotApprovedProjectWithinThreeDays(String email, String businessOwnerFullName, String navigatorFirstName, String advisorFullName, String projectId, String threeDaysLater) {
        String templatePath = EmailTemplatePathsConstants.REMINDER_NOT_APPROVED_PROJECT;
        String title = MessageHelper.getMessage(MessageHelper.getMessageWithCode("email.reminder.project.notApproved.title"), businessOwnerFullName);
        HashMap<String, Object> mapContents = new HashMap<>();
        mapContents.put(NAVIGATOR_FIRST_NAME, navigatorFirstName);
        mapContents.put(ADVISOR_FULL_NAME, advisorFullName);
        mapContents.put(THREE_DAYS_LATER, threeDaysLater);
        mailService.sendEmailFromTemplate(email, AppConstants.DEFAULT_LANGUAGE, mapContents, templatePath, title);
    }

    /**
     * Build Portal URL
     *
     * @param portal Portal
     * @return String
     */
    public String buildPortalUrl(Portal portal) {
        String clientAppUrl = applicationProperties.getClientApp().getBaseUrl();
        if (!ObjectUtils.isEmpty(portal) && !ObjectUtils.isEmpty(portal.getUrl())) {
            String baseUrl = Boolean.TRUE.equals(portal.getIsCustomDomain())
                ? applicationProperties.getClientApp().getBaseCustomUrlPortal()
                : applicationProperties.getClientApp().getBaseUrlPortal();
            clientAppUrl = String.format(baseUrl, portal.getUrl());
        }

        return clientAppUrl;
    }

    public  String generateUsername(String firstName, String lastName) {
        String raw = (firstName + lastName).toLowerCase().replaceAll("\\s+", StringUtils.EMPTY);
        String noSpecialChars = raw.replaceAll("[^a-z0-9]", StringUtils.EMPTY);
        String baseUsername = removeDiacritics(noSpecialChars);

        if (!userRepository.existsByUsername(baseUsername)) {
            return baseUsername;
        }
        int suffix = 1;
        String candidate;
        do {
            candidate = baseUsername + String.format("%02d", suffix++);
        } while (userRepository.existsByUsername(candidate));

        return candidate;
    }

    private  String removeDiacritics(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private void prepareRequestForInvitation(RequestInviteMember request, User currentUser) {
        request.setInvitationOrganization(currentUser.getOrganization());
        request.setInviteToken(RandomUtil.generateResetKey());
    }

    private void sendInvitationEmailToTechnicalAdvisor(RequestInviteMember request, User currentUser,
                                                       String guestEmail, TechnicalAdvisor technicalAdvisor) {
        String senderName = String.join(" ", currentUser.getFirstName(), currentUser.getLastName());

        if (!ObjectUtils.isEmpty(request.getPortalIds())) {
            sendInvitationEmail(senderName, guestEmail, request.toFullName(), request.getInviteToken(), TECHNICAL_ADVISOR,
                currentUser.getOrganization(), technicalAdvisor.getPortals().stream().toList());
        }
    }

    private String getPortalUrl(UUID portalId) {
        return Objects.isNull(portalId) ? null : getPortal(portalId).getUrl();
    }

    private TechnicalAdvisor saveTechnicalAdvisorInvite(RequestInviteMember request, CommunityPartner communityPartner) {
        User newUser = createUserFromRequest(request, AuthoritiesConstants.TECHNICAL_ADVISOR);
        newUser = userRepository.save(newUser);
        insertUserSettings(newUser.getId());

        TechnicalAdvisor technicalAdvisor = createTechnicalAdvisor(newUser, communityPartner);

        if (!ObjectUtils.isEmpty(request.getPortalIds())) {
            technicalAdvisor.setPortals(getListPortals(request.getPortalIds()));
        }

        technicalAdvisor = technicalAdvisorRepository.save(technicalAdvisor);

        return technicalAdvisor;
    }


    private User createUserFromRequest(RequestInviteMember request, String role) {
        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(role).ifPresent(authorities::add);
        String encryptedPassword = userHelper.genRandomPassword();
        return User.builder()
            .login(request.getEmail())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .activated(false)
            .password(encryptedPassword)
            .username(generateUsername(request.getFirstName(), request.getLastName()))
            .status(UserStatusEnum.INVITED)
            .authorities(authorities)
            .organization(request.getInvitationOrganization())
            .resetKey(request.getInviteToken())
            .resetDate(Instant.now().plus(BusinessConstant.NUMBER_90, ChronoUnit.DAYS))
            .build();
    }

    private TechnicalAdvisor createTechnicalAdvisor(User user, CommunityPartner communityPartner) {
        return TechnicalAdvisor.builder()
            .user(user)
            .communityPartner(communityPartner)
            .build();
    }

    /**
     * Invite CommunityPartner Staff
     *
     * @param request            RequestInviteMember
     * @param communityPartnerId UUID
     * @return UUID
     */
    public UUID inviteCommunityPartnerStaff(RequestInviteMember request, UUID communityPartnerId) {
        String guestEmail = request.getEmail();

        // Check if the user exists and validate their association
        var existingStaff = getExistsCommunityPartnerStaff(guestEmail);
        validateExistingCommunityPartnerAssociation(existingStaff);

        // Fetch the community partner and portals
        CommunityPartner communityPartner = getCommunityPartner(communityPartnerId);
        var portals = Objects.requireNonNull(communityPartner).getPortals();

        UUID userId = existingStaff.getId();
        if (Boolean.FALSE.equals(existingStaff.getIsExist())) {
            userId = handleNewCommunityPartnerStaffInvite(request, communityPartner);
        }
        if (Boolean.TRUE.equals(request.getIsNavigator())) {
            userRepository.updateUserNavigator(communityPartnerId, userId);
            communityPartner.setStatus(StatusEnum.INACTIVE);
            communityPartnerRepository.save(communityPartner);
        }
        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            userRepository.updateUserPrimary(communityPartnerId, userId);
        }

        // Send invitation email
        sendInvitationCommunityPartner(
            guestEmail,
            request.toFullName(),
            request.getInviteToken(),
            portals,
            MEMBER_COMMUNITY_PARTNER,
            existingStaff.getIsExist(),
            communityPartner.getName()
        );

        return userId;
    }

    private void validateExistingCommunityPartnerAssociation(ResponseExistsCommunityPartnerStaff existingStaff) {
        if (Boolean.TRUE.equals(existingStaff.getIsExist())
            && Objects.nonNull(existingStaff.getCommunityPartnerName())) {
            throw new BadRequestException(MessageHelper.getMessage(
                Message.Keys.E0091,
                existingStaff.getCommunityPartnerName()
            ));
        }
    }

    public UUID handleNewCommunityPartnerStaffInvite(RequestInviteMember request, CommunityPartner communityPartner) {
        User currentUser = getCurrentLoginUser();
        request.setInvitationOrganization(currentUser.getOrganization());
        request.setInviteToken(RandomUtil.generateResetKey());
        return saveCommunityPartnerStaffInvite(request, communityPartner);
    }



    /**
     * Get Exists CommunityPartner Staff
     *
     * @param email String
     * @return ResponseExistsCommunityPartnerStaff
     */
    public ResponseExistsCommunityPartnerStaff getExistsCommunityPartnerStaff(String email) {
        var response = ResponseExistsCommunityPartnerStaff.builder().isExist(false).build();
        if (Objects.isNull(email)) {
            return response;
        }
        return userRepository.findOneByEmail(email)
            .map(user -> {
                validateCommunityPartnerRole(user);
                return buildResponse(user);
            })
            .orElse(response);
    }

    private void validateCommunityPartnerRole(User user) {
        boolean isRoleCommunityPartner = user.getAuthorities()
            .stream()
            .map(Authority::getName)
            .anyMatch(AuthoritiesConstants.COMMUNITY_PARTNER::equals);

        if (!isRoleCommunityPartner) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0049));
        }
    }

    private ResponseExistsCommunityPartnerStaff buildResponse(User user) {
        ResponseExistsCommunityPartnerStaff response =
            ResponseExistsCommunityPartnerStaff.builder().isExist(true).build();
        BeanUtils.copyProperties(user, response);
        if (Objects.nonNull(user.getCommunityPartner())) {
            response.setCommunityPartnerName(user.getCommunityPartner().getName());
        }
        return response;
    }

    /**
     * Save CommunityPartner Staff Invite
     *
     * @param request RequestInviteMember
     * @return UUID
     */
    private UUID saveCommunityPartnerStaffInvite(RequestInviteMember request, CommunityPartner communityPartner) {
        User user = createUserFromRequest(request, AuthoritiesConstants.COMMUNITY_PARTNER);
        user.setCommunityPartner(communityPartner);
        user.setIsNavigator(request.getIsNavigator());
        user.setIsPrimary(request.getIsPrimary());
        userRepository.save(user);
        UUID userId = user.getId();
        insertUserSettings(userId);

        return userId;
    }

    private User getCurrentLoginUser() {
        String login = SecurityUtils.getCurrentUserLogin().orElse("");
        return userRepository
            .findOneByLogin(login)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "User login")));
    }

    /**
     * Validates if a user with the given email already exists in the system.
     *
     * @param email The email of the user to validate.
     * @throws BadRequestException if a user with the specified email already exists.
     */
    private void validateExistingUser(String email) {
        var existsByLogin = userRepository.existsByLoginIgnoreCaseIncludeDeleted(email);
        if (Boolean.TRUE.equals(existsByLogin)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "Email"));
        }
    }

    private void validateExistingUserCommunityPartner(String email) {
        var existsByLogin = userRepository.findOneByEmailIgnoreCase(email);
        if (existsByLogin.isPresent() && Objects.nonNull(existsByLogin.get().getCommunityPartner())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "Email"));
        }
    }

    private CommunityPartner getCommunityPartner(UUID id) {
        return communityPartnerRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Community Partner")));
    }

    private Portal getPortal(UUID id) {
        return portalRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Portal")));
    }

    private Set<Portal> getListPortals(List<UUID> portalIds) {
        var portals = portalRepository.findAllById(portalIds);
        return new HashSet<>(portals);
    }

    /**
     * Insert User Settings
     *
     * @param userId UUID
     */
    public void insertUserSettings(UUID userId) {
        userRepository.insertUserSettings(userId);
    }
}
