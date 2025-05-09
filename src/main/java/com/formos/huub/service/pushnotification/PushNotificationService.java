package com.formos.huub.service.pushnotification;

import com.formos.huub.config.ApplicationProperties;
import com.formos.huub.domain.constant.EmailTemplatePathsConstants;
import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.*;
import com.formos.huub.domain.record.AppointmentNotificationData;
import com.formos.huub.domain.record.NotificationTemplate;
import com.formos.huub.framework.constant.AppConstants;
import com.formos.huub.framework.context.PortalContext;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.enums.DateTimeFormat;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.properties.SystemConfigurationProperties;
import com.formos.huub.framework.utils.DateUtils;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.repository.ConversationRepository;
import com.formos.huub.repository.PortalHostRepository;
import com.formos.huub.repository.PortalRepository;
import com.formos.huub.repository.UserRepository;
import com.formos.huub.security.SecurityUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.formos.huub.framework.constant.AppConstants.*;

@Service
@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class PushNotificationService {

    private static final String DIRECT_MESSAGE_URL = "/messages/conversation?conversationId=";
    private static final String PROFILE_DETAIL_URL = "/members/detail/";
    private static final String COMMUNITY_BOARD_GROUP_PENDING_REQUEST_URL = "community-boards/group/%s/%s?tab=manage&requestId=%s";
    private static final String COMMUNITY_BOARD_POST_VIEW_URL = "/community-boards/posts/%s?type=POST&postId=%s";
    private static final String COMMUNITY_BOARD_POST_VIEW_IN_GROUP_URL = "/community-boards/group/%s/%s?type=POST&postId=%s";
    private static final String COMMUNITY_BOARD_COMMENT_VIEW_URL = "/community-boards/posts/%s?type=COMMENT&postId=%s&commentId=%s";
    private static final String COMMUNITY_BOARD_COMMENT_IN_GROUP_URL = "/community-boards/group/%s/%s?type=COMMENT&postId=%s&commentId=%s";
    private static final String COMMUNITY_BOARD_FLAG_VIEW_URL = "/community-boards/posts/%s?type=FLAG&id=%s";
    private static final String COMMUNITY_BOARD_JOIN_GROUP_URL = "/community-boards/group/%s/%s?isAcceptInvite=true";
    private static final String COMMUNITY_BOARD_GROUP_URL = "/community-boards/group/%s/%s";
    private static final String APPOINTMENT_MANAGER_TA_URL = "/ta-managements/appointment-management?appointmentId=%s";
    private static final String APPOINTMENT_MANAGER_BO_URL = "/manage-1-1-support?appointmentId=%s";
    private static final String FOOTER_LINK = "/profile-info/edit";

    private static final String APPOINTMENT_DATE_AND_TIME = "appointmentDateAndTime";
    private static final String BUSINESS_OWNER_NAME = "businessOwnerName";
    private static final String APPOINTMENT_ID = "appointmentId";
    private static final String ADVISOR_MEETING_LINK = "advisorMeetingLink";
    private static final String ADVISOR_EMAIL = "advisorEmail";
    private static final String ADVISOR_NAME = "advisorName";

    private static final String SUPPORT_LIVE_CHAT = "supportLiveChat";
    private static final String SUPPORT_EMAIL = "supportEmail";
    private static final String APPOINTMENT_DETAIL_URL = "appointmentDetailUrl";

    private static final String USER_ID = "userId";
    private static final String PROGRAM_MANAGER_NAME = "programManagerName";
    private static final String PROGRAM_MANAGER_EMAIL = "programManagerEmail";
    private static final String HOURS_AWARD = "hoursAward";
    private static final String VENDOR_NAME = "vendorName";
    private static final String NAVIGATOR_NAME = "navigatorName";
    private static final String NAVIGATOR_EMAIL = "navigatorEmail";
    private static final String ADVISOR = "advisor";

    @Value("${schedule.timezone}")
    private String timeZoneId;

    private ZoneId configuredZone;

    @PostConstruct
    public void init() {
        this.configuredZone = ZoneId.of(timeZoneId);
    }

    private final EmailNotificationStrategy emailStrategy;
    private final WebNotificationStrategy webStrategy;
    private final SmsStrategy smsStrategy;
    private final UserRepository userRepository;
    private final PortalRepository portalRepository;
    private final ConversationRepository conversationRepository;
    private final ApplicationProperties applicationProperties;
    private final SystemConfigurationProperties systemConfiguration;
    private final PortalHostRepository portalHostRepository;

    public void sendNewFollowerForMember(User followed) {
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        var portalInfo = getPortalInfo();
        var baseReferenceUrl = PROFILE_DETAIL_URL.concat(currentUser.getId().toString());
        var referenceUrl = portalInfo.get(PORTAL_URL).concat(baseReferenceUrl);
        var senderName = WordUtils.capitalize(currentUser.getNormalizedFullName());
        var receiverName = WordUtils.capitalize(followed.getNormalizedFullName());
        HashMap<String, Object> contents = new HashMap<>();
        contents.put("buttonHref", referenceUrl);
        contents.put("buttonText", "View Member");

        var context = NotificationContext.builder()
            .title(buildFollowerNotificationTitle(senderName))
            .content(buildFollowerNotificationContent(senderName, receiverName, referenceUrl))
            .baseReferenceUrl(baseReferenceUrl)
            .referenceUrl(referenceUrl)
            .notificationType(NotificationTypeEnum.NEW_FOLLOWER)
            .recipientIds(List.of(followed.getId()))
            .portalId(PortalContextHolder.getPortalId())
            .sender(currentUser)
            .portalInfo(portalInfo)
            .settingKeyCode(SettingKeyCodeEnum.MEMBER_STARTS_FOLLOWING.getValue())
            .templatePath(EmailTemplatePathsConstants.MEMBER_RECEIVE_NEW_FOLLOWER)
            .emailTitle(MessageHelper.getMessage("email.follow.member.new.follower.title", portalInfo.get(PORTAL_NAME), senderName))
            .additionalData(contents)
            .build();

        emailStrategy.processNotification(context);
        webStrategy.processNotification(context);
    }

    public void sendMentionNotification(List<UUID> userIds, UUID conversationId) {
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        var portalInfo = getPortalInfo();
        var baseReferenceUrl = DIRECT_MESSAGE_URL.concat(conversationId.toString());
        var memberUrl = portalInfo.get(PORTAL_URL).concat(PROFILE_DETAIL_URL.concat(currentUser.getId().toString()));
        var referenceUrl = portalInfo.get(PORTAL_URL).concat(baseReferenceUrl);
        var senderName = WordUtils.capitalize(currentUser.getNormalizedFullName());
        HashMap<String, Object> contents = new HashMap<>();
        contents.put("buttonHref", referenceUrl);
        contents.put("buttonText", "View Message");
        contents.put("memberUrl", memberUrl);

        var context = NotificationContext.builder()
            .title(MessageHelper.getMessageWithCode("notification.direct.message.mention.title"))
            .content(MessageHelper.getMessage(MessageHelper.getMessageWithCode("email.direct.message.mention.notification"), senderName))
            .baseReferenceUrl(baseReferenceUrl)
            .referenceUrl(referenceUrl)
            .notificationType(NotificationTypeEnum.NEW_MENTION)
            .recipientIds(userIds)
            .portalId(PortalContextHolder.getPortalId())
            .sender(currentUser)
            .portalInfo(portalInfo)
            .settingKeyCode(SettingKeyCodeEnum.MEMBER_MENTION.getValue())
            .templatePath(EmailTemplatePathsConstants.MENTION_IN_MESSAGE)
            .emailTitle(MessageHelper.getMessage("email.direct.message.mention.title", portalInfo.get(PORTAL_NAME), senderName))
            .additionalData(contents)
            .build();

        emailStrategy.processNotification(context);
        webStrategy.processNotification(context);
    }

    public void sendNotifyAllForPostFromAdmin(CommunityBoardPost post, String userPostName, List<UUID> userIds) {
        var portalInfo = getPortalInfo(post.getPortalId());
        var memberUrl = portalInfo.get(PORTAL_URL).concat(PROFILE_DETAIL_URL.concat(post.getAuthorId().toString()));
        String baseReferenceUrl;
        if (CommunityBoardVisibilityEnum.GROUP.equals(post.getVisibility())) {
            baseReferenceUrl = String.format(
                COMMUNITY_BOARD_POST_VIEW_IN_GROUP_URL,
                post.getPortalId().toString(),
                post.getGroupId().toString(),
                post.getId().toString()
            );
        } else {
            baseReferenceUrl = String.format(COMMUNITY_BOARD_POST_VIEW_URL, post.getPortalId().toString(), post.getId().toString());
        }
        var referenceUrl = portalInfo.get(PORTAL_URL).concat(baseReferenceUrl);
        var sender = userRepository
            .findById(post.getAuthorId())
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Author Post")));
        var senderName = WordUtils.capitalize(sender.getNormalizedFullName());
        userPostName = WordUtils.capitalize(userPostName);
        HashMap<String, Object> contents = new HashMap<>();
        contents.put("buttonHref", referenceUrl);
        contents.put("buttonText", "View Post");
        contents.put("memberUrl", memberUrl);
        contents.put("communityBoard", AppConstants.COMMUNITY_BOARD);

        var context = NotificationContext.builder()
            .title(buildPostNotifyAllTitle(userPostName))
            .content(buildPostNotifyAllContent(userPostName, portalInfo.get(PORTAL_NAME), referenceUrl))
            .baseReferenceUrl(baseReferenceUrl)
            .referenceUrl(referenceUrl)
            .notificationType(NotificationTypeEnum.POST_NOTIFY_ALL)
            .recipientIds(userIds)
            .portalId(post.getPortalId())
            .sender(sender)
            .portalInfo(portalInfo)
            .settingKeyCode(SettingKeyCodeEnum.NEW_POST_MEMBER_FOLLOWING.getValue())
            .templatePath(EmailTemplatePathsConstants.NEW_ACTIVITY_POST_NOTIFY_ALL_BY_ADMIN)
            .emailTitle(MessageHelper.getMessage("email.activity.post.notifyAll.title", portalInfo.get(PORTAL_NAME), senderName))
            .description(post.getContent())
            .additionalData(contents)
            .build();

        emailStrategy.processNotification(context);
        webStrategy.processNotification(context);
    }

    public void sendPostNotificationToFollowers(
        List<CommunityBoardPost> posts,
        List<UUID> followerIds,
        String userPostName,
        String groupName
    ) {
        posts.forEach(post -> sendPostNotificationToFollowers(post, followerIds, userPostName, groupName));
    }

    public void sendPostNotificationToFollowers(CommunityBoardPost post, List<UUID> followerIds, String userPostName, String groupName) {
        var portalInfo = getPortalInfo(post.getPortalId());
        String baseReferenceUrl;
        String groupUrl;
        var memberUrl = portalInfo.get(PORTAL_URL).concat(PROFILE_DETAIL_URL.concat(post.getAuthorId().toString()));
        if (CommunityBoardVisibilityEnum.GROUP.equals(post.getVisibility())) {
            baseReferenceUrl = String.format(
                COMMUNITY_BOARD_POST_VIEW_IN_GROUP_URL,
                post.getPortalId().toString(),
                post.getGroupId().toString(),
                post.getId().toString()
            );
            groupUrl = portalInfo.get(PORTAL_URL).concat(String.format(COMMUNITY_BOARD_GROUP_URL, post.getPortalId().toString(), post.getGroupId().toString()));
        } else {
            baseReferenceUrl = String.format(COMMUNITY_BOARD_POST_VIEW_URL, post.getPortalId().toString(), post.getId().toString());
            groupUrl = portalInfo.get(PORTAL_URL).concat(baseReferenceUrl);
        }
        var referenceUrl = portalInfo.get(PORTAL_URL).concat(baseReferenceUrl);
        var sender = userRepository
            .findById(post.getAuthorId())
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Author Post")));
        var senderName = WordUtils.capitalize(sender.getNormalizedFullName());
        userPostName = WordUtils.capitalize(userPostName);
        HashMap<String, Object> contents = new HashMap<>();
        contents.put("groupName", groupName);
        contents.put("buttonHref", referenceUrl);
        contents.put("buttonText", "View Post");
        contents.put("memberUrl", memberUrl);
        contents.put("groupUrl", groupUrl);

        var context = NotificationContext.builder()
            .title(buildPostToFollowersTitle(userPostName, groupName))
            .content(buildPostToFollowersContent(userPostName, groupName, referenceUrl))
            .baseReferenceUrl(baseReferenceUrl)
            .referenceUrl(referenceUrl)
            .notificationType(NotificationTypeEnum.NEW_ACTIVITY_POSTED_FOLLOWER)
            .recipientIds(followerIds)
            .portalId(post.getPortalId())
            .sender(sender)
            .portalInfo(portalInfo)
            .settingKeyCode(SettingKeyCodeEnum.NEW_POST_MEMBER_FOLLOWING.getValue())
            .templatePath(EmailTemplatePathsConstants.NEW_ACTIVITY_POST_FOLLOWER)
            .emailTitle(MessageHelper.getMessage("email.activity.post.following.title", portalInfo.get(PORTAL_NAME), senderName))
            .description(post.getContent())
            .additionalData(contents)
            .build();

        emailStrategy.processNotification(context);
        webStrategy.processNotification(context);
    }

    public void sendInviteJoinGroupToMember(CommunityBoardGroup group, String inviteMessage, List<UUID> memberIds) {
        var portalInfo = getPortalInfo(group.getPortalId());
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        var memberUrl = portalInfo.get(PORTAL_URL).concat(PROFILE_DETAIL_URL.concat(currentUser.getId().toString()));
        var baseReferenceUrl = String.format(COMMUNITY_BOARD_JOIN_GROUP_URL, group.getPortalId().toString(), group.getId().toString());
        var referenceUrl = portalInfo.get(PORTAL_URL).concat(baseReferenceUrl);
        var senderName = WordUtils.capitalize(currentUser.getNormalizedFullName());
        HashMap<String, Object> contents = new HashMap<>();
        contents.put("groupName", group.getGroupName());
        contents.put("inviteMessage", inviteMessage);
        contents.put("memberUrl", memberUrl);

        var context = NotificationContext.builder()
            .title(buildInviteJoinGroupTitle(group.getGroupName()))
            .content(buildInviteJoinGroupContent(senderName, group.getGroupName(), inviteMessage))
            .baseReferenceUrl(baseReferenceUrl)
            .referenceUrl(referenceUrl)
            .notificationType(NotificationTypeEnum.GROUP_INVITATIONS_REQUEST)
            .recipientIds(memberIds)
            .portalId(group.getPortalId())
            .sender(currentUser)
            .portalInfo(portalInfo)
            .settingKeyCode(SettingKeyCodeEnum.RECEIVE_INVITE_JOIN_GROUP.getValue())
            .templatePath(EmailTemplatePathsConstants.NEW_INVITE_JOIN_GROUP_TO_MEMBER)
            .emailTitle(MessageHelper.getMessage("email.invite.join.group.title", portalInfo.get(PORTAL_NAME), group.getGroupName()))
            .additionalData(contents)
            .build();

        emailStrategy.processNotification(context);
        webStrategy.processNotification(context);
    }

    public void sendNotificationWhenPromoteMemberInGroup(CommunityBoardGroup group, UUID memberId, CommunityBoardGroupRoleEnum groupRole) {
        var portalInfo = getPortalInfo(group.getPortalId());
        var baseReferenceUrl = String.format(COMMUNITY_BOARD_GROUP_URL, group.getPortalId().toString(), group.getId().toString());
        var referenceUrl = portalInfo.get(PORTAL_URL).concat(baseReferenceUrl);
        boolean promotedModerator = CommunityBoardGroupRoleEnum.MODERATOR.equals(groupRole);
        String promotedToRole = groupRole.getName();
        HashMap<String, Object> contents = new HashMap<>();
        contents.put("groupName", group.getGroupName());
        contents.put("groupRole", promotedToRole);
        contents.put("buttonHref", referenceUrl);
        contents.put("buttonText", "View Group");

        var context = NotificationContext.builder()
            .title(buildPromotedMemberInGroupTitle(group.getGroupName(), promotedToRole))
            .content(buildPromotedMemberInGroupContent(group.getGroupName(), baseReferenceUrl, promotedToRole))
            .baseReferenceUrl(baseReferenceUrl)
            .referenceUrl(referenceUrl)
            .notificationType(promotedModerator ? NotificationTypeEnum.PROMOTED_MODERATOR : NotificationTypeEnum.PROMOTED_ORGANIZER)
            .recipientIds(List.of(memberId))
            .portalId(group.getPortalId())
            .portalInfo(portalInfo)
            .settingKeyCode(SettingKeyCodeEnum.PROMOTED_IN_GROUP.getValue())
            .templatePath(EmailTemplatePathsConstants.NEW_PROMOTE_MEMBER_IN_GROUP)
            .emailTitle(MessageHelper.getMessage("email.promoted.group.title", portalInfo.get(PORTAL_NAME), group.getGroupName()))
            .additionalData(contents)
            .build();

        emailStrategy.processNotification(context);
        webStrategy.processNotification(context);
    }

    public void sendNotificationRequestJoinGroupAccepted(CommunityBoardGroup group, UUID memberId) {
        var portalInfo = getPortalInfo(group.getPortalId());
        var baseReferenceUrl = String.format(COMMUNITY_BOARD_GROUP_URL, group.getPortalId().toString(), group.getId().toString());
        var referenceUrl = portalInfo.get(PORTAL_URL).concat(baseReferenceUrl);
        var groupUrl = portalInfo.get(PORTAL_URL).concat(String.format(COMMUNITY_BOARD_GROUP_URL, group.getPortalId().toString(), group.getId().toString()));
        HashMap<String, Object> contents = new HashMap<>();
        contents.put("groupName", group.getGroupName());
        contents.put("groupUrl", groupUrl);
        contents.put("buttonHref", referenceUrl);
        contents.put("buttonText", "View Group");

        var context = NotificationContext.builder()
            .title(buildRequestJoinGroupAcceptedTitle(group.getGroupName()))
            .content(buildRequestJoinGroupAcceptedContent(group.getGroupName(), baseReferenceUrl))
            .baseReferenceUrl(baseReferenceUrl)
            .referenceUrl(referenceUrl)
            .notificationType(NotificationTypeEnum.ACCEPTED_JOIN_GROUP_REQUEST)
            .recipientIds(List.of(memberId))
            .portalId(group.getPortalId())
            .portalInfo(portalInfo)
            .settingKeyCode(SettingKeyCodeEnum.REQUEST_JOIN_GROUP_ACCEPTED.getValue())
            .templatePath(EmailTemplatePathsConstants.NEW_REQUEST_JOIN_GROUP_ACCEPTED)
            .emailTitle(MessageHelper.getMessage("email.join.group.accepted.title", group.getGroupName(), null))
            .additionalData(contents)
            .build();

        emailStrategy.processNotification(context);
        webStrategy.processNotification(context);
    }

    public void sendNotificationRequestJoinGroupRejected(CommunityBoardGroup group, UUID memberId) {
        var portalInfo = getPortalInfo(group.getPortalId());
        var groupUrl = portalInfo.get(PORTAL_URL).concat(String.format(COMMUNITY_BOARD_GROUP_URL, group.getPortalId().toString(), group.getId().toString()));
        HashMap<String, Object> contents = new HashMap<>();
        contents.put("groupName", group.getGroupName());
        contents.put("groupUrl", groupUrl);

        var context = NotificationContext.builder()
            .title(buildRequestJoinGroupRejectedTitle(group.getGroupName()))
            .content(buildRequestJoinGroupRejectedContent(group.getGroupName()))
            .notificationType(NotificationTypeEnum.REJECTED_JOIN_GROUP_REQUEST)
            .recipientIds(List.of(memberId))
            .portalId(group.getPortalId())
            .portalInfo(portalInfo)
            .settingKeyCode(SettingKeyCodeEnum.REQUEST_JOIN_GROUP_REJECTED.getValue())
            .templatePath(EmailTemplatePathsConstants.NEW_REQUEST_JOIN_GROUP_REJECTED)
            .emailTitle(MessageHelper.getMessage("email.join.group.rejected.title", group.getGroupName(), null))
            .additionalData(contents)
            .build();

        emailStrategy.processNotification(context);
        webStrategy.processNotification(context);
    }

    public void sendNotificationPostInGroup(String groupName, CommunityBoardPost post, String postAuthor, List<UUID> memberIds) {
        var portalInfo = getPortalInfo(post.getPortalId());
        String baseReferenceUrl;
        String groupUrl;
        var memberUrl = portalInfo.get(PORTAL_URL).concat(PROFILE_DETAIL_URL.concat(post.getAuthorId().toString()));
        if (CommunityBoardVisibilityEnum.GROUP.equals(post.getVisibility())) {
            baseReferenceUrl = String.format(
                COMMUNITY_BOARD_POST_VIEW_IN_GROUP_URL,
                post.getPortalId().toString(),
                post.getGroupId().toString(),
                post.getId().toString()
            );
            groupUrl = portalInfo.get(PORTAL_URL).concat(String.format(COMMUNITY_BOARD_GROUP_URL, post.getPortalId().toString(), post.getGroupId().toString()));
        } else {
            baseReferenceUrl = String.format(COMMUNITY_BOARD_POST_VIEW_URL, post.getPortalId().toString(), post.getId().toString());
            groupUrl = portalInfo.get(PORTAL_URL).concat(baseReferenceUrl);
        }
        var referenceUrl = portalInfo.get(PORTAL_URL).concat(baseReferenceUrl);
        HashMap<String, Object> contents = new HashMap<>();
        contents.put("groupName", groupName);
        contents.put("postAuthor", postAuthor);
        contents.put("buttonHref", referenceUrl);
        contents.put("buttonText", "View Post");
        contents.put("memberUrl", memberUrl);
        contents.put("groupUrl", groupUrl);

        var context = NotificationContext.builder()
            .title(buildActivityPostGroupTitle(postAuthor, groupName))
            .content(buildActivityPostGroupTitleContent(postAuthor, groupName, referenceUrl, post.getContent()))
            .notificationType(NotificationTypeEnum.NEW_POST)
            .baseReferenceUrl(baseReferenceUrl)
            .referenceUrl(referenceUrl)
            .recipientIds(memberIds)
            .portalId(post.getPortalId())
            .portalInfo(portalInfo)
            .description(post.getContent())
            .settingKeyCode(SettingKeyCodeEnum.NEW_POST_IN_GROUP_SUBSCRIBED.getValue())
            .templatePath(EmailTemplatePathsConstants.NEW_NOTIFICATION_POST_IN_GROUP)
            .emailTitle(MessageHelper.getMessage("email.activity.post.group.title", portalInfo.get(PORTAL_NAME), postAuthor, groupName))
            .additionalData(contents)
            .build();

        emailStrategy.processNotification(context);
        webStrategy.processNotification(context);
    }

    public void sendNotificationUpdateManageInGroup(CommunityBoardGroup group, List<UUID> memberIds) {
        var portalInfo = getPortalInfo(group.getPortalId());
        var baseReferenceUrl = String.format(COMMUNITY_BOARD_GROUP_URL, group.getPortalId().toString(), group.getId().toString());
        var referenceUrl = portalInfo.get(PORTAL_URL).concat(baseReferenceUrl);
        HashMap<String, Object> contents = new HashMap<>();
        contents.put("groupName", group.getGroupName());
        contents.put("groupDescription", group.getDescription());
        contents.put("buttonHref", referenceUrl);
        contents.put("buttonText", "View Group");

        var context = NotificationContext.builder()
            .title(buildUpdateManageGroupTitle(group.getGroupName()))
            .content(buildUpdateManageGroupContent(group.getGroupName(), group.getDescription(), referenceUrl))
            .notificationType(NotificationTypeEnum.UPDATE_MANAGE_GROUP)
            .baseReferenceUrl(baseReferenceUrl)
            .referenceUrl(referenceUrl)
            .recipientIds(memberIds)
            .portalId(group.getPortalId())
            .portalInfo(portalInfo)
            .settingKeyCode(SettingKeyCodeEnum.NEW_POST_IN_GROUP_SUBSCRIBED.getValue())
            .templatePath(EmailTemplatePathsConstants.NEW_UPDATE_MANAGE_IN_GROUP)
            .emailTitle(MessageHelper.getMessage("email.update.manage.group.title", portalInfo.get(PORTAL_NAME), group.getGroupName()))
            .additionalData(contents)
            .build();

        emailStrategy.processNotification(context);
        webStrategy.processNotification(context);
    }

    public void sendJoinGroupRequestToManager(CommunityBoardGroup group, User manager, UUID requestId) {
        var portalInfo = getPortalInfo(group.getPortalId());
        var requestUser = SecurityUtils.getCurrentUser(userRepository);
        var baseReferenceUrl = String.format(
            COMMUNITY_BOARD_GROUP_PENDING_REQUEST_URL,
            group.getPortalId().toString(),
            group.getId().toString(),
            requestId.toString()
        );
        var memberUrl = portalInfo.get(PORTAL_URL).concat(PROFILE_DETAIL_URL.concat(requestUser.getId().toString()));
        var groupUrl = portalInfo.get(PORTAL_URL).concat(String.format(COMMUNITY_BOARD_GROUP_URL, group.getPortalId().toString(), group.getId().toString()));
        var referenceUrl = portalInfo.get(PORTAL_URL).concat(baseReferenceUrl);
        var requestUsername = WordUtils.capitalize(requestUser.getNormalizedFullName());
        var managerUserName = WordUtils.capitalize(manager.getNormalizedFullName());
        HashMap<String, Object> contents = new HashMap<>();
        contents.put("groupName", group.getGroupName());
        contents.put("memberUrl", memberUrl);
        contents.put("groupUrl", groupUrl);

        var context = NotificationContext.builder()
            .title(buildRequestJoinGroupTitle(requestUsername, group.getGroupName()))
            .content(buildRequestJoinGroupContent(requestUsername, managerUserName, group.getGroupName()))
            .baseReferenceUrl(baseReferenceUrl)
            .referenceUrl(referenceUrl)
            .notificationType(NotificationTypeEnum.POST_NOTIFY_ALL)
            .recipientIds(List.of(manager.getId()))
            .portalId(group.getPortalId())
            .sender(requestUser)
            .portalInfo(portalInfo)
            .settingKeyCode(SettingKeyCodeEnum.MEMBER_REQUEST_JOIN_GROUP.getValue())
            .templatePath(EmailTemplatePathsConstants.NEW_REQUEST_JOIN_GROUP_BY_MEMBER)
            .emailTitle(
                MessageHelper.getMessage(
                    "email.request.join.group.title",
                    portalInfo.get(PORTAL_NAME),
                    requestUsername,
                    group.getGroupName()
                )
            )
            .additionalData(contents)
            .build();

        emailStrategy.processNotification(context);
        webStrategy.processNotification(context);
    }

    public void sendNotifyFlagPostForManager(
        UUID flagId,
        String reason,
        List<UUID> managerIds,
        Map<String, String> portalInfo,
        UUID portalId
    ) {
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        var baseReferenceUrl = String.format(COMMUNITY_BOARD_FLAG_VIEW_URL, portalId.toString(), flagId.toString());
        var referenceUrl = portalInfo.get(PORTAL_URL).concat(baseReferenceUrl);
        var senderName = WordUtils.capitalize(currentUser.getNormalizedFullName());

        var context = NotificationContext.builder()
            .title(MessageHelper.getMessageWithCode("email.activity.flag.post.title"))
            .content(buildFlagPostContent(senderName, portalInfo.get(PORTAL_NAME), reason, referenceUrl))
            .baseReferenceUrl(baseReferenceUrl)
            .referenceUrl(referenceUrl)
            .notificationType(NotificationTypeEnum.FLAG_POST_NOTIFY)
            .recipientIds(managerIds)
            .portalId(portalId)
            .sender(currentUser)
            .portalInfo(portalInfo)
            .settingKeyCode(SettingKeyCodeEnum.ENABLE_NOTIFICATIONS.getValue())
            .templatePath(EmailTemplatePathsConstants.MEMBER_FLAG_POST_FOR_MANAGER)
            .emailTitle(MessageHelper.getMessage("email.activity.flag.post.title", portalInfo.get(PORTAL_NAME), senderName))
            .description(reason)
            .build();

        emailStrategy.processNotification(context);
        webStrategy.processNotification(context);
    }

    public void sendNotifyFlagCommentForManager(
        UUID flagId,
        String reason,
        List<UUID> managerIds,
        Map<String, String> portalInfo,
        UUID portalId
    ) {
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        var senderName = WordUtils.capitalize(currentUser.getNormalizedFullName());
        var baseReferenceUrl = String.format(COMMUNITY_BOARD_FLAG_VIEW_URL, portalId.toString(), flagId.toString());
        var referenceUrl = portalInfo.get(PORTAL_URL).concat(baseReferenceUrl);

        var context = NotificationContext.builder()
            .title(MessageHelper.getMessageWithCode("email.activity.flag.comment.title"))
            .content(buildFlagCommentContent(senderName, portalInfo.get(PORTAL_NAME), reason, referenceUrl))
            .baseReferenceUrl(baseReferenceUrl)
            .referenceUrl(referenceUrl)
            .notificationType(NotificationTypeEnum.FLAG_POST_NOTIFY)
            .recipientIds(managerIds)
            .portalId(portalId)
            .sender(currentUser)
            .portalInfo(portalInfo)
            .settingKeyCode(SettingKeyCodeEnum.ENABLE_NOTIFICATIONS.getValue())
            .templatePath(EmailTemplatePathsConstants.MEMBER_FLAG_COMMENT_FOR_MANAGER)
            .emailTitle(MessageHelper.getMessage("email.activity.flag.comment.title", portalInfo.get(PORTAL_NAME), senderName))
            .description(reason)
            .build();

        emailStrategy.processNotification(context);
        webStrategy.processNotification(context);
    }

    public void sendNotifyWhenMemberReplyComment(
        CommunityBoardComment comment,
        User parentComment,
        List<CommunityBoardFile> files,
        CommunityBoardPost post
    ) {
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        var senderName = WordUtils.capitalize(currentUser.getNormalizedFullName());
        var receiverName = WordUtils.capitalize(parentComment.getNormalizedFullName());
        var portalInfo = getPortalInfo(post.getPortalId());
        String baseReferenceUrl;
        var memberUrl = portalInfo.get(PORTAL_URL).concat(PROFILE_DETAIL_URL.concat(comment.getAuthorId().toString()));
        if (CommunityBoardVisibilityEnum.GROUP.equals(post.getVisibility())) {
            baseReferenceUrl = String.format(
                COMMUNITY_BOARD_COMMENT_IN_GROUP_URL,
                post.getPortalId().toString(),
                post.getGroupId().toString(),
                comment.getPostId().toString(),
                comment.getId().toString()
            );
        } else {
            baseReferenceUrl = String.format(
                COMMUNITY_BOARD_COMMENT_VIEW_URL,
                post.getPortalId().toString(),
                comment.getPostId().toString(),
                comment.getId().toString()
            );
        }

        var referenceUrl = portalInfo.get(PORTAL_URL).concat(baseReferenceUrl);
        HashMap<String, Object> contents = new HashMap<>();
        contents.put("buttonHref", referenceUrl);
        contents.put("buttonText", "View Comment");
        contents.put("memberUrl", memberUrl);

        var context = NotificationContext.builder()
            .title(buildReplyCommentTitle(senderName))
            .content(buildReplyCommentContent(senderName, receiverName, referenceUrl, comment.getContent()))
            .baseReferenceUrl(baseReferenceUrl)
            .referenceUrl(referenceUrl)
            .notificationType(NotificationTypeEnum.NEW_COMMENT)
            .recipientIds(List.of(parentComment.getId()))
            .portalId(post.getPortalId())
            .sender(currentUser)
            .portalInfo(portalInfo)
            .settingKeyCode(SettingKeyCodeEnum.MEMBER_REPLIES_POST_COMMENT.getValue())
            .templatePath(EmailTemplatePathsConstants.MEMBER_RECEIVE_NEW_REPLY_COMMENT)
            .emailTitle(MessageHelper.getMessage("email.reply.comment.notify.title", portalInfo.get(PORTAL_NAME), senderName))
            .description(comment.getContent())
            .additionalData(contents)
            .build();

        emailStrategy.processNotification(context);
        webStrategy.processNotification(context);
    }

    public void sendNotifyWhenMemberCommentOnPost(
        CommunityBoardComment comment,
        User postUser,
        List<CommunityBoardFile> files,
        CommunityBoardPost post
    ) {
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        var senderName = WordUtils.capitalize(currentUser.getNormalizedFullName());
        var receiverName = WordUtils.capitalize(postUser.getNormalizedFullName());
        var portalInfo = getPortalInfo(post.getPortalId());
        var memberUrl = portalInfo.get(PORTAL_URL).concat(PROFILE_DETAIL_URL.concat(comment.getAuthorId().toString()));
        String baseReferenceUrl;
        if (CommunityBoardVisibilityEnum.GROUP.equals(post.getVisibility())) {
            baseReferenceUrl = String.format(
                COMMUNITY_BOARD_COMMENT_IN_GROUP_URL,
                post.getPortalId().toString(),
                post.getGroupId().toString(),
                comment.getPostId().toString(),
                comment.getId().toString()
            );
        } else {
            baseReferenceUrl = String.format(
                COMMUNITY_BOARD_COMMENT_VIEW_URL,
                post.getPortalId().toString(),
                comment.getPostId().toString(),
                comment.getId().toString()
            );
        }

        var referenceUrl = portalInfo.get(PORTAL_URL).concat(baseReferenceUrl);
        HashMap<String, Object> contents = new HashMap<>();
        contents.put("buttonHref", referenceUrl);
        contents.put("buttonText", "View Comment");
        contents.put("memberUrl", memberUrl);
        var context = NotificationContext.builder()
            .title(buildCommentOnPostTitle(senderName))
            .content(buildCommentOnPostContent(senderName, receiverName, referenceUrl, comment.getContent()))
            .baseReferenceUrl(baseReferenceUrl)
            .referenceUrl(referenceUrl)
            .notificationType(NotificationTypeEnum.NEW_COMMENT)
            .recipientIds(List.of(postUser.getId()))
            .portalId(post.getPortalId())
            .sender(currentUser)
            .portalInfo(portalInfo)
            .settingKeyCode(SettingKeyCodeEnum.MEMBER_REPLIES_POST_COMMENT.getValue())
            .templatePath(EmailTemplatePathsConstants.MEMBER_RECEIVE_NEW_COMMENT_POST)
            .emailTitle(MessageHelper.getMessage("email.activity.comment.notify.title", portalInfo.get(PORTAL_NAME), senderName))
            .description(comment.getContent())
            .additionalData(contents)
            .build();

        emailStrategy.processNotification(context);
        webStrategy.processNotification(context);
    }

    public void sendNotifyWhenBOCompleteSurveyPostAppointment(Appointment appointment) {
        var technicalAdvisor = appointment.getTechnicalAdvisor().getUser();
        var businessOwner = appointment.getUser();
        var portalInfo = getPortalInfo(appointment.getPortal());

        HashMap<String, Object> contents = new HashMap<>();
        contents.put("businessOwner", businessOwner.getNormalizedFullName());
        contents.put("technicalAdvisor", technicalAdvisor.getNormalizedFullName());
        contents.put("appointmentDateAndTime", formatAppointmentDateTime(appointment));
        contents.put("rateValue", appointment.getAppointmentDetail().getRating());
        contents.put("feedback", appointment.getAppointmentDetail().getFeedback());
        var context = NotificationContext.builder()
            .recipientEmail(List.of(systemConfiguration.getMailSlack()))
            .templatePath(EmailTemplatePathsConstants.RESULT_APPOINTMENT_SURVEY_EMAIL)
            .emailTitle(
                MessageHelper.getMessage("email.result.appointment.survey.notify.title", businessOwner.getNormalizedFullName(), null)
            )
            .additionalData(contents)
            .portalInfo(portalInfo)
            .build();

        emailStrategy.processNotification(context);
    }

    public void sendNotifyWhenRestrictMemberPost(User user) {
        var portalInfo = getPortalInfo();
        var sender = SecurityUtils.getCurrentUser(userRepository);
        var senderName = WordUtils.capitalize(sender.getNormalizedFullName());
        var receiverName = WordUtils.capitalize(user.getNormalizedFullName());

        var context = NotificationContext.builder()
            .title(buildRestrictMemberTitle())
            .content(buildRestrictMemberContent(receiverName))
            .notificationType(NotificationTypeEnum.RESTRICT_MEMBER)
            .recipientIds(List.of(user.getId()))
            .portalId(PortalContextHolder.getPortalId())
            .sender(sender)
            .portalInfo(portalInfo)
            .settingKeyCode(SettingKeyCodeEnum.MEMBER_REPLIES_POST_COMMENT.getValue())
            .templatePath(EmailTemplatePathsConstants.RESTRICT_MEMBER_POST_COMMENT)
            .emailTitle(MessageHelper.getMessage("email.restrict.user.notify.title", portalInfo.get(PORTAL_NAME), senderName))
            .build();

        emailStrategy.processNotification(context);
        webStrategy.processNotification(context);
    }

    public void sendMentionNotificationForComment(
        List<UUID> userIds,
        CommunityBoardComment comment,
        List<CommunityBoardFile> files,
        CommunityBoardPost post
    ) {
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        var senderName = WordUtils.capitalize(currentUser.getNormalizedFullName());
        var portalInfo = getPortalInfo(post.getPortalId());
        String baseReferenceUrl;
        var memberUrl = portalInfo.get(PORTAL_URL).concat(PROFILE_DETAIL_URL.concat(post.getAuthorId().toString()));
        if (CommunityBoardVisibilityEnum.GROUP.equals(post.getVisibility())) {
            baseReferenceUrl = String.format(
                COMMUNITY_BOARD_COMMENT_IN_GROUP_URL,
                post.getPortalId().toString(),
                post.getGroupId().toString(),
                comment.getPostId().toString(),
                comment.getId().toString()
            );
        } else {
            baseReferenceUrl = String.format(
                COMMUNITY_BOARD_COMMENT_VIEW_URL,
                post.getPortalId().toString(),
                comment.getPostId().toString(),
                comment.getId().toString()
            );
        }

        var referenceUrl = portalInfo.get(PORTAL_URL).concat(baseReferenceUrl);
        HashMap<String, Object> contents = new HashMap<>();
        contents.put("buttonHref", referenceUrl);
        contents.put("buttonText", "View Comment");
        contents.put("memberUrl", memberUrl);
        var context = NotificationContext.builder()
            .title(buildMentionMemberInCommentTitle(senderName))
            .content(buildMentionMemberInCommentContent(senderName, baseReferenceUrl, comment.getContent()))
            .baseReferenceUrl(baseReferenceUrl)
            .referenceUrl(referenceUrl)
            .notificationType(NotificationTypeEnum.NEW_MENTION)
            .recipientIds(userIds)
            .portalId(post.getPortalId())
            .sender(currentUser)
            .portalInfo(portalInfo)
            .settingKeyCode(SettingKeyCodeEnum.MEMBER_MENTION.getValue())
            .templatePath(EmailTemplatePathsConstants.NEW_MENTION_COMMENT_FOR_MEMBER)
            .emailTitle(MessageHelper.getMessage("email.direct.comment.mention.title", portalInfo.get(PORTAL_NAME), senderName))
            .description(comment.getContent())
            .additionalData(contents)
            .build();

        emailStrategy.processNotification(context);
        webStrategy.processNotification(context);
    }

    public void sendMentionNotificationForListPost(List<UUID> userIds, List<CommunityBoardPost> posts, List<CommunityBoardFile> files) {
        posts.forEach(post -> sendMentionNotificationForPost(userIds, post, files));
    }

    public void sendMentionNotificationForPost(List<UUID> userIds, CommunityBoardPost post, List<CommunityBoardFile> files) {
        var currentUser = userRepository
            .findById(post.getAuthorId())
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Author Post")));
        var senderName = WordUtils.capitalize(currentUser.getNormalizedFullName());
        var portalInfo = getPortalInfo(post.getPortalId());
        String baseReferenceUrl;
        var memberUrl = portalInfo.get(PORTAL_URL).concat(PROFILE_DETAIL_URL.concat(post.getAuthorId().toString()));
        if (CommunityBoardVisibilityEnum.GROUP.equals(post.getVisibility())) {
            baseReferenceUrl = String.format(
                COMMUNITY_BOARD_POST_VIEW_IN_GROUP_URL,
                post.getPortalId().toString(),
                post.getGroupId().toString(),
                post.getId().toString()
            );
        } else {
            baseReferenceUrl = String.format(COMMUNITY_BOARD_POST_VIEW_URL, post.getPortalId().toString(), post.getId().toString());
        }
        var referenceUrl = portalInfo.get(PORTAL_URL).concat(baseReferenceUrl);
        HashMap<String, Object> contents = new HashMap<>();
        contents.put("buttonHref", referenceUrl);
        contents.put("buttonText", "View Post");
        contents.put("memberUrl", memberUrl);

        var context = NotificationContext.builder()
            .title(buildMentionMemberInPostTitle(senderName))
            .content(buildMentionMemberInPostContent(senderName, baseReferenceUrl))
            .baseReferenceUrl(baseReferenceUrl)
            .referenceUrl(referenceUrl)
            .notificationType(NotificationTypeEnum.NEW_MENTION)
            .recipientIds(userIds)
            .portalId(post.getPortalId())
            .sender(currentUser)
            .portalInfo(portalInfo)
            .settingKeyCode(SettingKeyCodeEnum.MEMBER_MENTION.getValue())
            .templatePath(EmailTemplatePathsConstants.NEW_MENTION_POST_FOR_MEMBER)
            .additionalData(contents)
            .emailTitle(MessageHelper.getMessage("email.direct.post.mention.title", portalInfo.get(PORTAL_NAME), senderName))
            .description(post.getContent())
            .build();

        emailStrategy.processNotification(context);
        webStrategy.processNotification(context);
    }

    public void sendNotifyToUserOfNewMessage(List<UUID> userIds, UUID conversationId){
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        sendNotifyToUserOfNewMessage(userIds, conversationId, currentUser);
    }

    public void sendNotifyToUserOfNewMessage(List<UUID> userIds, UUID conversationId, User sender) {
        var senderName = WordUtils.capitalize(sender.getNormalizedFullName());
        var baseReferenceUrl = DIRECT_MESSAGE_URL.concat(conversationId.toString());

        Conversation conversation = conversationRepository
            .findById(conversationId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Conversation")));

        var settingKeyCode = conversation.getConversationType() == ConversationTypeEnum.SINGLE
            ? SettingKeyCodeEnum.RECEIVE_NEW_PRIVATE_MESSAGE.getValue()
            : SettingKeyCodeEnum.RECEIVE_NEW_GROUP_MESSAGE.getValue();

        var context = NotificationContext.builder()
            .title(MessageHelper.getMessage("notification.direct.message.new.title", senderName, null))
            .content(MessageHelper.getMessage("notification.direct.message.new.title", senderName, null))
            .baseReferenceUrl(baseReferenceUrl)
            .notificationType(NotificationTypeEnum.NEW_MESSAGE)
            .recipientIds(userIds)
            .portalId(PortalContextHolder.getPortalId())
            .sender(sender)
            .settingKeyCode(settingKeyCode)
            .build();

        webStrategy.processNotification(context);
    }

    public void sendNotifyToUserOfChangedPassword(UUID userId) {
        var context = NotificationContext.builder()
            .title(MessageHelper.getMessageWithCode("notification.reset.title"))
            .content(MessageHelper.getMessageWithCode("notification.reset.title"))
            .notificationType(NotificationTypeEnum.PASSWORD_CHANGED)
            .recipientIds(List.of(userId))
            .portalId(PortalContextHolder.getPortalId())
            .settingKeyCode(SettingKeyCodeEnum.PASSWORD_CHANGED.getValue())
            .build();

        webStrategy.processNotification(context);
    }

    public String sendSmsApprovalForBusinessOwner(Boolean isApproved) {
        String contentSmsApproved = MessageHelper.getMessageWithCode("sms.applications.business.owner.approved.title");
        String contentSmsDenied = MessageHelper.getMessageWithCode("sms.applications.business.owner.denied.title");
        return isApproved ? contentSmsApproved : contentSmsDenied;
    }


    public void sendSmsProjectNotApprovedForBusinessOwner(User user, String advisorFullName, String portalName) {
        String contentSmsProjectNotApproved = MessageHelper.getMessage(
            "sms.project.business.owner.approved.content",
            advisorFullName,
            portalName
        );

        var context = NotificationContext.builder()
            .recipientIds(List.of(user.getId()))
            .sender(user)
            .content(contentSmsProjectNotApproved)
            .build();
        smsStrategy.processNotification(context);
    }

    public void sendScheduleAppointmentMailForBusinessOwner(Appointment appointment) {
        sendScheduleAppointmentNotification(appointment, false);
    }

    public void sendScheduleAppointmentMailForAdvisor(Appointment appointment) {
        sendScheduleAppointmentNotification(appointment, true);
    }

    public void sendCancelAppointmentMailForBusinessOwner(Appointment appointment) {
        sendCancelAppointmentNotification(appointment, false);
    }

    public void sendCancelAppointmentMailApprovedForAdvisor(Appointment appointment) {
        sendCancelAppointmentNotification(appointment, true);
    }

    public void sendRescheduleAppointmentMailForBusinessOwner(Appointment appointment) {
        sendRescheduleAppointmentNotification(appointment, false);
    }

    public void sendRescheduleAppointmentMailApprovedForAdvisor(Appointment appointment) {
        sendRescheduleAppointmentNotification(appointment, true);
    }

    public void sendUpdateAppointmentMailForBusinessOwner(Appointment appointment) {
        sendUpdateAppointmentNotification(appointment, false);
    }

    public void sendUpdateAppointmentMailApprovedForAdvisor(Appointment appointment) {
        sendUpdateAppointmentNotification(appointment, true);
    }

    public void sendPostAppointmentMailForBusinessOwner(Appointment appointment) {
        var recipient = appointment.getUser();
        User advisor = appointment.getTechnicalAdvisor().getUser();
        String fullName = getFullName(advisor);
        String appointmentDateAndTime = formatAppointmentDateTime(appointment);
        String templatePath = EmailTemplatePathsConstants.POST_APPOINTMENT_EMAIL_TO_BUSINESS_OWNER;
        String title = MessageHelper.getMessage("email.24-hour-post.appointment.title", List.of(fullName));
        String titleReplace = String.join("", "[", fullName, "]");
        title = title.replace(titleReplace, fullName);
        HashMap<String, Object> contents = createAppointmentContents(appointment, appointmentDateAndTime, advisor);
        var portalInfo = getPortalInfo(appointment.getPortal().getId());
        var context = NotificationContext.builder()
            .recipientIds(List.of(recipient.getId()))
            .sender(advisor)
            .emailTitle(title)
            .templatePath(templatePath)
            .additionalData(contents)
            .portalInfo(portalInfo)
            .build();
        emailStrategy.processNotification(context);
    }

    public void sendPostAppointmentReportReminderMailForAdvisor(Appointment appointment, long reminderDays) {
        var recipient = appointment.getTechnicalAdvisor().getUser();
        String templatePath = getReminderTemplate(reminderDays);
        if (templatePath != null) {
            String fullName = getFullName(appointment.getUser());
            String appointmentDateAndTime = formatAppointmentDateTime(appointment);
            String title = getReminderEmailTitle(reminderDays, fullName, appointmentDateAndTime);
            HashMap<String, Object> contents = createAppointmentContents(appointment, appointmentDateAndTime, recipient);
            var portalInfo = getPortalInfo(appointment.getPortal().getId());
            var context = NotificationContext.builder()
                .recipientIds(List.of(recipient.getId()))
                .sender(appointment.getUser())
                .emailTitle(title)
                .templatePath(templatePath)
                .additionalData(contents)
                .portalInfo(portalInfo)
                .build();
            emailStrategy.processNotification(context);
        }
    }

    /**
     * Builds and sends a reminder email for a project's status report using a template.
     * This method constructs the context for the email, including recipient information,
     * email template, and additional details about the project. The email is sent to the
     * project's technical advisor.
     *
     * @param project           The project for which the reminder email is being generated.
     * @param emailTemplatePath The file path of the template used for the email content.
     * @param emailTitleCode    The code used to retrieve the title of the email message.
     */
    public void buildTemplateReminderProjectReport(Project project, String emailTemplatePath, String emailTitleCode) {
        final User advisor = project.getTechnicalAdvisor().getUser();
        final User businessOwner = project.getBusinessOwner().getUser();
        final Portal portal = project.getPortal();

        final String advisorEmail = advisor.getEmail();

        ZonedDateTime completionDate = ZonedDateTime.ofInstant(project.getEstimatedCompletionDate(), this.configuredZone);
        String formattedCompletionDate = completionDate.format(DateTimeFormatter.ofPattern(DateTimeFormat.MM_DD_YYYY.getValue()));

        String linkToProjectReport =
            buildPortalUrl(portal) + "/business-owners/card?projectId=" + project.getId() + "&featureCode=PROJECT_REPORT";

        Map<String, Object> emailContext = Map.of(
            "advisorFirstName",
            advisor.getFirstName(),
            "projectName",
            project.getProjectName(),
            "businessOwnerFullName",
            getFullName(businessOwner),
            "estimatedCompletionDate",
            formattedCompletionDate,
            "linkToProjectReport",
            linkToProjectReport
        );
        var portalInfo = getPortalInfo(project.getPortal().getId());
        NotificationContext context = NotificationContext.builder()
            .recipientIds(List.of(advisor.getId()))
            .emailTitle(MessageHelper.getMessageWithCode(emailTitleCode))
            .templatePath(emailTemplatePath)
            .additionalData(emailContext)
            .portalInfo(portalInfo)
            .build();

        emailStrategy.processNotification(context);
    }

    public void buildTemplateReminderProject(Project project, String emailTemplatePath, String emailTitleCode) {
        final User advisor = project.getTechnicalAdvisor().getUser();
        final User businessOwner = project.getBusinessOwner().getUser();
        final Portal portal = project.getPortal();

        ZonedDateTime threeDaysLater = ZonedDateTime.now(this.configuredZone).plusDays(3);
        String formattedThreeDaysLater = threeDaysLater.format(DateTimeFormatter.ofPattern(DateTimeFormat.MM_DD_YYYY.getValue()));
        var portalInfo = getPortalInfo(project.getPortal().getId());
        String linkToProjectDetail = buildPortalUrl(portal) + "/ta-managements/project-management?projectId=" + project.getId();

        userRepository
            .findCommunityPartnerNavigatorById(project.getVendor().getId())
            .ifPresent(navigatorUser -> {
                Map<String, Object> emailContext = Map.of(
                    "businessOwnerFullName",
                    getFullName(businessOwner),
                    "navigatorFirstName",
                    navigatorUser.getFirstName(),
                    "advisorFullName",
                    getFullName(advisor),
                    "threeDaysLater",
                    formattedThreeDaysLater,
                    "linkToProjectDetail",
                    linkToProjectDetail
                );
                NotificationContext context = NotificationContext.builder()
                    .recipientIds(List.of(advisor.getId()))
                    .emailTitle(MessageHelper.getMessage(emailTitleCode, getFullName(businessOwner), null))
                    .templatePath(emailTemplatePath)
                    .additionalData(emailContext)
                    .portalInfo(portalInfo)
                    .build();

                emailStrategy.processNotification(context);
            });
    }

    public void buildTemplateProjectReport(Project project, String emailTitleCode) {

        String emailTemplatePath = EmailTemplatePathsConstants.PROJECT_REPORT;

        String postProjectFeedbackFormLink = buildPortalUrl(project.getPortal()) + "/project/" + project.getId() + "/feedback";
        User businessOwnerUser = project.getBusinessOwner().getUser();
        String supportLiveChat = applicationProperties.getCustomerCare().getJoinHuubLiveChat();
        var portalInfo = getPortalInfo(project.getPortal().getId());
        Map<String, Object> emailContext = Map.of(
            "businessOwnerFirstName",
            businessOwnerUser.getFirstName(),
            "postProjectFeedbackFormLink",
            postProjectFeedbackFormLink,
            "supportLiveChat",
            supportLiveChat
        );

        // Build notification context
        NotificationContext context = NotificationContext.builder()
            .recipientIds(List.of(businessOwnerUser.getId()))
            .emailTitle(MessageHelper.getMessage(emailTitleCode, project.getProjectName(), null))
            .templatePath(emailTemplatePath)
            .additionalData(emailContext)
            .portalInfo(portalInfo)
            .build();

        // Process notifications
        emailStrategy.processNotification(context);
    }

    public void buildTemplateReminderPreAppointment(Appointment appointment, String emailTitleCode, boolean isForAdvisor) {
        // Get relevant users and portal
        User businessOwnerUser = appointment.getUser();
        User advisorUser = appointment.getTechnicalAdvisor().getUser();
        Portal portal = appointment.getPortal();
        User recipient = isForAdvisor ? advisorUser : businessOwnerUser;

        // Prepare common data
        ZoneId zoneId = ZoneId.of(appointment.getTimezone());
        String appointmentDateTimeStr = appointment
            .getAppointmentDate()
            .atZone(zoneId)
            .format(DateTimeFormatter.ofPattern(DateTimeFormat.MM_dd_yyyy_hh_mm_a.getValue()));

        // Select appropriate template path based on recipient
        String emailTemplatePath = isForAdvisor
            ? EmailTemplatePathsConstants.HOURS_24_PRE_APPOINTMENT_REMINDER_EMAIL_TO_ADVISOR
            : EmailTemplatePathsConstants.HOURS_24_PRE_APPOINTMENT_REMINDER_EMAIL_TO_BUSINESS_OWNER;

        // Build email context
        Map<String, Object> emailContext = buildEmailContext(appointment, portal, advisorUser, businessOwnerUser, appointmentDateTimeStr);

        // Prepare SMS content (only for business owner)
        String smsContent = !isForAdvisor
            ? MessageHelper.getMessage(
            "sms.businessOwner.preAppointment.content",
            advisorUser.getNormalizedFullName(),
            appointmentDateTimeStr,
            portal.getPlatformName()
        )
            : null;

        // Create email title
        String emailTitle = isForAdvisor
            ? MessageHelper.getMessage(emailTitleCode, businessOwnerUser.getNormalizedFullName(), appointmentDateTimeStr)
            : MessageHelper.getMessage(emailTitleCode, advisorUser.getNormalizedFullName(), appointmentDateTimeStr);
        var portalInfo = getPortalInfo(appointment.getPortal().getId());
        // Build notification context
        NotificationContext context = NotificationContext.builder()
            .recipientIds(List.of(recipient.getId()))
            .sender(recipient)
            .emailTitle(emailTitle)
            .templatePath(emailTemplatePath)
            .additionalData(emailContext)
            .content(smsContent)
            .portalInfo(portalInfo)
            .build();

        // Process notifications
        emailStrategy.processNotification(context);
        if (!isForAdvisor) {
            smsStrategy.processNotification(context);
        }
    }

    private Map<String, Object> buildEmailContext(
        Appointment appointment,
        Portal portal,
        User advisorUser,
        User businessOwnerUser,
        String appointmentDateTimeStr
    ) {
        String linkToAppointment = buildPortalUrl(portal) + "/ta-managements/appointment-management?appointmentId=" + appointment.getId();

        String linkToAppointmentForBusinessOwner = buildPortalUrl(portal) + "/manage-1-1-support?appointmentId=" + appointment.getId();

        String advisorMeetingLink = "";
        if (Objects.nonNull(advisorUser.getBookingSetting())) {
            advisorMeetingLink = advisorUser.getBookingSetting().getLinkMeetingPlatform();
        }

        String supportLiveChat = applicationProperties.getCustomerCare().getJoinHuubLiveChat();

        return Map.of(
            "advisorFullName",
            advisorUser.getNormalizedFullName(),
            "advisorFirstName",
            advisorUser.getFirstName(),
            "appointmentDateTime",
            appointmentDateTimeStr,
            "businessOwnerFirstName",
            businessOwnerUser.getFirstName(),
            "businessOwnerFullName",
            businessOwnerUser.getNormalizedFullName(),
            "advisorMeetingLink",
            advisorMeetingLink,
            "portalName",
            portal.getPlatformName(),
            "linkToAppointment",
            linkToAppointment,
            "linkToAppointmentForBusinessOwner",
            linkToAppointmentForBusinessOwner,
            "supportLiveChat",
            supportLiveChat
        );
    }

    public void sendMailApprovedForBusinessOwner(TechnicalAssistanceSubmit technicalAssistanceSubmit, CommunityPartner communityPartner) {
        var portal = technicalAssistanceSubmit.getPortal();
        var emailTitle = MessageHelper.getMessage("email.applications.business.owner.approved.title", List.of(StringUtils.EMPTY));
        var emailTemplatePath = EmailTemplatePathsConstants.APPROVED_APPLICATION_FOR_BUSINESS_OWNER;
        var businessOwner = technicalAssistanceSubmit.getUser();
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        var smsContent = sendSmsApprovalForBusinessOwner(true);
        HashMap<String, Object> mapContents = new HashMap<>();
        mapContents.put(HOURS_AWARD, technicalAssistanceSubmit.getAssignAwardHours().toString());
        mapContents.put(VENDOR_NAME, communityPartner.getName());
        addNavigatorDetails(communityPartner.getId(), mapContents);
        var portalInfo = getPortalInfo(portal.getId());
        NotificationContext context = NotificationContext.builder()
            .recipientIds(List.of(businessOwner.getId()))
            .sender(currentUser)
            .emailTitle(emailTitle)
            .templatePath(emailTemplatePath)
            .additionalData(mapContents)
            .content(smsContent)
            .portalInfo(portalInfo)
            .build();

        // Process notifications
        emailStrategy.processNotification(context);
        smsStrategy.processNotification(context);
    }

    public void sendMailAssignAdvisorApplicationForBusinessOwner(TechnicalAssistanceSubmit technicalAssistanceSubmit, CommunityPartner communityPartner, List<String> advisorNames) {
        var portal = technicalAssistanceSubmit.getPortal();
        var emailTitle = MessageHelper.getMessage("email.applications.business.owner.assign.advisor.title", List.of(StringUtils.EMPTY));
        var emailTemplatePath = EmailTemplatePathsConstants.ASSIGN_ADVISOR_APPLICATION_FOR_BUSINESS_OWNER;
        var businessOwner = technicalAssistanceSubmit.getUser();
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        String smsContent = MessageHelper.getMessageWithCode("sms.applications.business.owner.assign.advisor.title");
        HashMap<String, Object> mapContents = new HashMap<>();
        addNavigatorDetails(communityPartner != null ? communityPartner.getId() : null, mapContents);
        AtomicInteger atomicInt = new AtomicInteger(0);
        advisorNames.forEach(advisor -> mapContents.put(ADVISOR.concat(String.valueOf(atomicInt.incrementAndGet())), advisor));
        mapContents.put(PORTAL_NAME, portal.getPlatformName());
        var portalInfo = getPortalInfo(portal.getId());
        NotificationContext context = NotificationContext.builder()
            .recipientIds(List.of(businessOwner.getId()))
            .sender(currentUser)
            .emailTitle(emailTitle)
            .templatePath(emailTemplatePath)
            .additionalData(mapContents)
            .content(smsContent)
            .portalInfo(portalInfo)
            .build();

        // Process notifications
        emailStrategy.processNotification(context);
        smsStrategy.processNotification(context);
    }

    public void sendMailApprovedForCommunityPartnerNavigator(TechnicalAssistanceSubmit technicalAssistanceSubmit, CommunityPartner vendor) {
        var navigatorOpt = userRepository.findCommunityPartnerNavigatorById(vendor.getId());
        if (navigatorOpt.isEmpty()) {
            return;
        }
        var navigator = navigatorOpt.get();
        HashMap<String, Object> mapContents = new HashMap<>();
        mapContents.put(USER_ID, Optional.ofNullable(technicalAssistanceSubmit.getUser()).map(User::getId).orElse(null));
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        var emailTitle = MessageHelper.getMessage("email.applications.community.partner.approved.title", vendor.getName(), null);
        var emailTemplatePath = EmailTemplatePathsConstants.APPROVED_APPLICATION_FOR_COMMUNITY_PARTNER_NAVIGATOR;
        var portalInfo = getPortalInfo(technicalAssistanceSubmit.getPortal().getId());
        NotificationContext context = NotificationContext.builder()
            .recipientIds(List.of(navigator.getId()))
            .sender(currentUser)
            .emailTitle(emailTitle)
            .templatePath(emailTemplatePath)
            .additionalData(mapContents)
            .portalInfo(portalInfo)
            .build();
        // Process notifications
        emailStrategy.processNotification(context);
    }

    public void sendMailProcessDeniedForBusinessOwner(TechnicalAssistanceSubmit technicalAssistanceSubmit) {
        var portal = technicalAssistanceSubmit.getPortal();
        var emailTitle = MessageHelper.getMessage("email.applications.business.owner.denied.title", List.of(StringUtils.EMPTY));
        var emailTemplatePath = EmailTemplatePathsConstants.DENIED_APPLICATION_FOR_BUSINESS_OWNER;
        var businessOwner = technicalAssistanceSubmit.getUser();
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        var smsContent = sendSmsApprovalForBusinessOwner(false);
        HashMap<String, Object> mapContents = new HashMap<>();
        addProgramManagerDetails(portal.getId(), mapContents);
        var portalInfo = getPortalInfo(portal.getId());
        NotificationContext context = NotificationContext.builder()
            .recipientIds(List.of(businessOwner.getId()))
            .sender(currentUser)
            .emailTitle(emailTitle)
            .templatePath(emailTemplatePath)
            .additionalData(mapContents)
            .content(smsContent)
            .portalInfo(portalInfo)
            .build();

        // Process notifications
        emailStrategy.processNotification(context);
        smsStrategy.processNotification(context);
    }

    /**
     * Utility Method to add Navigator details
     *
     * @param communityPartnerId UUID
     * @param mapContents        HashMap<String, Object>
     */
    private void addNavigatorDetails(UUID communityPartnerId, HashMap<String, Object> mapContents) {
        if (communityPartnerId != null) {
            userRepository.findCommunityPartnerNavigatorById(communityPartnerId).ifPresent(navigator -> {
                mapContents.put(NAVIGATOR_NAME, navigator.getNormalizedFullName());
                mapContents.put(NAVIGATOR_EMAIL, navigator.getEmail());
            });
        }
    }

    /**
     * Utility Method to add Program Manager details
     *
     * @param portalId    UUID
     * @param mapContents HashMap<String, Object>
     */
    private void addProgramManagerDetails(UUID portalId, HashMap<String, Object> mapContents) {
        portalHostRepository.findPortalHostPrimaryByPortal(portalId).ifPresent(manager -> {
            mapContents.put(PROGRAM_MANAGER_NAME, manager.getNormalizedFullName());
            mapContents.put(PROGRAM_MANAGER_EMAIL, manager.getEmail());
        });
    }

    public String buildPortalUrl(Portal portal) {
        String portalUrl = portal.getUrl();
        String clientAppUrl = applicationProperties.getClientApp().getBaseUrl();
        if (!ObjectUtils.isEmpty(portalUrl)) {
            String baseUrl = Boolean.TRUE.equals(portal.getIsCustomDomain())
                ? applicationProperties.getClientApp().getBaseCustomUrlPortal()
                : applicationProperties.getClientApp().getBaseUrlPortal();
            clientAppUrl = String.format(baseUrl, portalUrl);
        }

        return clientAppUrl;
    }

    /**
     * Sends a cancellation notification for an appointment to the recipient.
     * The notification can be sent to either the advisor or the user who created the appointment,
     * depending on the specified `isForAdvisor` parameter. This method constructs the relevant
     * notification content, including email and optionally SMS, and processes it through the
     * appropriate notification strategies.
     *
     * @param appointment  The appointment that is being canceled, containing details such as
     *                     the date, time, and related users (advisor and business owner).
     * @param isForAdvisor A boolean indicating the notification recipient. If {@code true},
     *                     the notification is sent to the advisor; otherwise, it is sent to
     *                     the business owner (user who created the appointment).
     */
    private void sendCancelAppointmentNotification(Appointment appointment, boolean isForAdvisor) {
        sendAppointmentNotification(appointment, isForAdvisor, NotificationTypeEnum.CANCEL_APPOINTMENT);
    }

    private void sendRescheduleAppointmentNotification(Appointment appointment, boolean isForAdvisor) {
        sendAppointmentNotification(appointment, isForAdvisor, NotificationTypeEnum.RESCHEDULE_APPOINTMENT);
    }

    private void sendUpdateAppointmentNotification(Appointment appointment, boolean isForAdvisor) {
        sendAppointmentNotification(appointment, isForAdvisor, NotificationTypeEnum.UPDATE_APPOINTMENT);
    }

    private void sendScheduleAppointmentNotification(Appointment appointment, boolean isForAdvisor) {
        sendAppointmentNotification(appointment, isForAdvisor, NotificationTypeEnum.SCHEDULE_APPOINTMENT);
    }

    private String getFooterLink(Map<String, String> portalInfo) {
        return portalInfo.get(PORTAL_URL).concat(FOOTER_LINK);
    }

    private String getFullName(User user) {
        return user.getNormalizedFullName();
    }

    private String formatAppointmentDateTime(Appointment appointment) {
        ZonedDateTime appointmentZoneDateAndTime = DateUtils.combineDateTimeWithZone(
            appointment.getAppointmentDate(),
            appointment.getTimezone()
        );
        return appointmentZoneDateAndTime.format(DateTimeFormatter.ofPattern(DateTimeFormat.MM_dd_yyyy_hh_mm_a.getValue()));
    }

    private HashMap<String, Object> createAppointmentContents(Appointment appointment, String appointmentDateAndTime, User advisorUser) {
        var portalInfo = getPortalInfo(appointment.getPortal());
        var businessOwnerUser = appointment.getUser();
        HashMap<String, Object> contents = new HashMap<>();
        contents.put(APPOINTMENT_DATE_AND_TIME, appointmentDateAndTime);
        contents.put(BUSINESS_OWNER_NAME, businessOwnerUser.getNormalizedFullName());
        contents.put(APPOINTMENT_ID, appointment.getId().toString());
        contents.put(BASE_URL, portalInfo.get(PORTAL_URL));
        String advisorMeetingLink = "";
        if (Objects.nonNull(advisorUser.getBookingSetting())) {
            advisorMeetingLink = advisorUser.getBookingSetting().getLinkMeetingPlatform();
        }
        contents.put(ADVISOR_MEETING_LINK, advisorMeetingLink);
        contents.put(ADVISOR_EMAIL, advisorUser.getEmail());
        contents.put(ADVISOR_NAME, advisorUser.getNormalizedFullName());
        contents.put(PORTAL_NAME, portalInfo.get(PORTAL_NAME));
        contents.put(SUPPORT_LIVE_CHAT, applicationProperties.getCustomerCare().getJoinHuubLiveChat());
        contents.put(SUPPORT_EMAIL, applicationProperties.getCustomerCare().getJoinHuubSupport());
        return contents;
    }

    private String getReminderTemplate(long reminderDays) {
        Map<Long, String> reminderTemplateMap = Map.of(
            1L,
            EmailTemplatePathsConstants.POST_APPOINTMENT_REPORT_REMINDER_EMAIL_TO_ADVISOR,
            3L,
            EmailTemplatePathsConstants.DAY_3_POST_APPOINTMENT_REPORT_REMINDER_EMAIL_TO_ADVISOR,
            7L,
            EmailTemplatePathsConstants.DAY_7_POST_APPOINTMENT_REPORT_REMINDER_EMAIL_TO_ADVISOR,
            14L,
            EmailTemplatePathsConstants.DAY_14_POST_APPOINTMENT_REPORT_REMINDER_EMAIL_TO_ADVISOR,
            30L,
            EmailTemplatePathsConstants.DAY_30_POST_APPOINTMENT_REPORT_REMINDER_EMAIL_TO_ADVISOR
        );
        return reminderTemplateMap.getOrDefault(reminderDays, null);
    }

    private String getReminderEmailTitle(long reminderDays, String fullName, String appointmentDateAndTime) {
        Map<Long, String> reminderTemplateMap = Map.of(
            3L,
            MessageHelper.getMessage("email.advisor.3-day-post.appointment.title", fullName, appointmentDateAndTime),
            7L,
            MessageHelper.getMessage("email.advisor.7-day-post.appointment.title", fullName, appointmentDateAndTime),
            14L,
            MessageHelper.getMessage("email.advisor.14-day-post.appointment.title", fullName, appointmentDateAndTime),
            30L,
            MessageHelper.getMessage("email.advisor.30-day-post.appointment.title", fullName, appointmentDateAndTime)
        );
        return reminderTemplateMap.getOrDefault(
            reminderDays,
            MessageHelper.getMessage("email.advisor.24-hour-post.appointment.title", fullName, appointmentDateAndTime)
        );
    }

    private AppointmentNotificationData prepareAppointmentNotificationData(
        Appointment appointment,
        boolean isForAdvisor,
        NotificationTypeEnum notificationType
    ) {
        String appointmentDateAndTime = formatAppointmentDateTime(appointment);
        User advisor = appointment.getTechnicalAdvisor().getUser();
        User recipient = isForAdvisor ? advisor : appointment.getUser();
        User sender = isForAdvisor ? appointment.getUser() : advisor;
        String baseReferenceUrl = String.format(
            isForAdvisor ? APPOINTMENT_MANAGER_TA_URL : APPOINTMENT_MANAGER_BO_URL,
            appointment.getId().toString()
        );

        NotificationTemplate template = getNotificationTemplate(notificationType, isForAdvisor);
        String senderFullName = getFullName(sender);

        String templatePath = template.templatePath();
        String title = MessageHelper.getMessage(template.titleKey(), senderFullName, appointmentDateAndTime);
        String contentSms = null;

        // SMS content is only needed for business owner notifications for certain types
        if (!isForAdvisor && template.hasSmsContent()) {
            contentSms = MessageHelper.getMessage(template.smsContentKey(), senderFullName, appointmentDateAndTime);
        }

        HashMap<String, Object> contents = createAppointmentContents(appointment, appointmentDateAndTime, advisor);
        contents.put(APPOINTMENT_DETAIL_URL, baseReferenceUrl);

        return new AppointmentNotificationData(recipient.getId(), sender, templatePath, baseReferenceUrl, contentSms, title, contents);
    }

    private void sendAppointmentNotification(Appointment appointment, boolean isForAdvisor, NotificationTypeEnum notificationType) {
        AppointmentNotificationData data = prepareAppointmentNotificationData(appointment, isForAdvisor, notificationType);
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        var portalInfo = getPortalInfo(appointment.getPortal().getId());
        NotificationContext context = NotificationContext.builder()
            .recipientIds(List.of(data.recipientId()))
            .sender(data.sender())
            .templatePath(data.templatePath())
            .emailTitle(data.title())
            .additionalData(data.contents())
            .content(data.contentSms())
            .baseReferenceUrl(data.baseReferenceUrl())
            .notificationType(notificationType)
            .title(data.title())
            .settingKeyCode(SettingKeyCodeEnum.ENABLE_NOTIFICATIONS.getValue())
            .portalId(appointment.getPortal().getId())
            .portalInfo(portalInfo)
            .build();

        if (Objects.nonNull(currentUser) && !currentUser.getId().equals(data.recipientId())) {
            webStrategy.processNotification(context);
        }
        emailStrategy.processNotification(context);

        // Only send SMS for canceled appointments to business owners
        if (
            (notificationType == NotificationTypeEnum.CANCEL_APPOINTMENT ||
                notificationType == NotificationTypeEnum.SCHEDULE_APPOINTMENT) &&
                !isForAdvisor
        ) {
            smsStrategy.processNotification(context);
        }
    }

    private NotificationTemplate getNotificationTemplate(NotificationTypeEnum notificationType, boolean isForAdvisor) {
        String templatePath;
        String titleKey = "email." + notificationType.getName().toLowerCase() + ".title";
        String smsContentKey = "email." + notificationType.getName().toLowerCase() + ".body";
        boolean hasSmsContent =
            notificationType == NotificationTypeEnum.CANCEL_APPOINTMENT ||
                notificationType == NotificationTypeEnum.RESCHEDULE_APPOINTMENT ||
                notificationType == NotificationTypeEnum.SCHEDULE_APPOINTMENT;

        if (isForAdvisor) {
            templatePath = switch (notificationType) {
                case CANCEL_APPOINTMENT -> EmailTemplatePathsConstants.CANCEL_APPOINTMENT_EMAIL_TO_ADVISOR;
                case RESCHEDULE_APPOINTMENT -> EmailTemplatePathsConstants.RESCHEDULE_APPOINTMENT_EMAIL_TO_ADVISOR;
                case UPDATE_APPOINTMENT -> EmailTemplatePathsConstants.UPDATE_APPOINTMENT_EMAIL_TO_ADVISOR;
                case SCHEDULE_APPOINTMENT -> EmailTemplatePathsConstants.SCHEDULE_APPOINTMENT_EMAIL_TO_ADVISOR;
                default -> throw new IllegalArgumentException("Unsupported notification type: " + notificationType);
            };
        } else {
            templatePath = switch (notificationType) {
                case CANCEL_APPOINTMENT -> EmailTemplatePathsConstants.CANCEL_APPOINTMENT_EMAIL_TO_BUSINESS_OWNER;
                case RESCHEDULE_APPOINTMENT ->
                    EmailTemplatePathsConstants.RESCHEDULE_APPOINTMENT_EMAIL_TO_BUSINESS_OWNER;
                case UPDATE_APPOINTMENT -> EmailTemplatePathsConstants.UPDATE_APPOINTMENT_EMAIL_TO_BUSINESS_OWNER;
                case SCHEDULE_APPOINTMENT -> EmailTemplatePathsConstants.SCHEDULE_APPOINTMENT_EMAIL_TO_BUSINESS_OWNER;
                default -> throw new IllegalArgumentException("Unsupported notification type: " + notificationType);
            };
        }

        return new NotificationTemplate(templatePath, titleKey, smsContentKey, hasSmsContent);
    }

    // ----------------- Helper methods Utils -----------------

    public Map<String, String> getPortalInfoForAdmin() {
        Map<String, String> portalInfo = new HashMap<>();
        portalInfo.put(PORTAL_NAME, "HUUB");
        portalInfo.put(PORTAL_URL, applicationProperties.getClientApp().getBaseUrl());
        return portalInfo;
    }

    public Map<String, String> getPortalInfo() {
        return getPortalInfo(Optional.empty());
    }

    public Map<String, String> getPortalInfo(UUID portalId) {
        return getPortalInfo(Optional.of(portalId));
    }

    public Map<String, String> getPortalInfo(Portal portal) {
        return getPortalInfoFromContext(Optional.ofNullable(portal));
    }

    private Map<String, String> getPortalInfo(Optional<UUID> portalId) {
        return getPortalInfoFromContext(portalId.flatMap(portalRepository::findById));
    }

    private Map<String, String> getPortalInfoFromContext(Optional<Portal> portalOpt) {
        Map<String, String> portalInfo = new HashMap<>();
        String portalName;
        String portalUrl;
        String portalLogo;

        if (portalOpt.isPresent()) {
            Portal portal = portalOpt.get();
            portalName = portal.getPlatformName();
            portalLogo = portal.getPrimaryLogo();

            if (Objects.isNull(portal.getUrl())) {
                portalUrl = applicationProperties.getClientApp().getBaseUrl();
            } else if (Boolean.TRUE.equals(portal.getIsCustomDomain())) {
                portalUrl = String.format(applicationProperties.getClientApp().getBaseCustomUrlPortal(), portal.getUrl());
            } else {
                portalUrl = String.format(applicationProperties.getClientApp().getBaseUrlPortal(), portal.getUrl());
            }
        } else {
            Optional<PortalContext> portalContext = Optional.ofNullable(PortalContextHolder.getContext());
            portalName = portalContext.map(PortalContext::getPlatformName).orElse("HUUB");
            portalUrl = portalContext
                .filter(portal -> !portal.isAdminAccess())
                .map(portal -> String.format(applicationProperties.getClientApp().getBaseCustomUrlPortal(), portal.getUrl()))
                .orElse(applicationProperties.getClientApp().getBaseUrl());
            portalLogo = portalContext.map(PortalContext::getPrimaryLogo).orElse(AppConstants.DEFAULT_PORTAL_LOGO);
        }

        portalInfo.put(PORTAL_NAME, portalName);
        portalInfo.put(PORTAL_URL, portalUrl);
        portalInfo.put(PORTAL_LOGO, portalLogo);

        return portalInfo;
    }

    // ----------------- Helper methods for building notification content -----------------

    private String buildFollowerNotificationTitle(String followerName) {
        return MessageHelper.getMessage("notification.follow.member.new.follower.title", followerName, null);
    }

    private String buildFollowerNotificationContent(String followerName, String followedName, String referenceUrl) {
        String contentBody1 = MessageHelper.getMessage("email.follow.member.new.follower.greeting", followedName, null);
        String contentBody2 = MessageHelper.getMessage("email.follow.member.new.follower.body", followerName, null);
        String contentBody3 = MessageHelper.getMessage("email.follow.member.new.follower.body2", referenceUrl, null);
        return contentBody1.concat("<br>").concat(contentBody2).concat("<br>").concat(contentBody3);
    }

    private String buildInviteJoinGroupTitle(String groupName) {
        return MessageHelper.getMessage("notification.invite.join.group.title", groupName, null);
    }

    private String buildInviteJoinGroupContent(String managerUserName, String groupName, String inviteMessage) {
        String greeting = MessageHelper.getMessage("email.invite.join.group.greeting", "{member}", null);
        String body = MessageHelper.getMessage("email.invite.join.group.body", managerUserName, groupName);
        String body1 = MessageHelper.getMessage("email.invite.join.group.body1", inviteMessage, null);
        String body2 = MessageHelper.getMessage("email.invite.join.group.body2", "{{{group.card}}}");
        String body3 = MessageHelper.getMessageWithCode("email.invite.join.group.body3");
        return greeting.concat("<br>").concat(body).concat("<br>").concat(body1).concat("<br>").concat(body2).concat("<br>").concat(body3);
    }

    private String buildRequestJoinGroupTitle(String requestUserName, String groupName) {
        return MessageHelper.getMessage("notification.request.join.group.title", requestUserName, groupName);
    }

    private String buildRequestJoinGroupContent(String requestUserName, String managerUserName, String groupName) {
        String greeting = MessageHelper.getMessage("email.request.join.group.greeting", managerUserName, null);
        String body = MessageHelper.getMessage("email.request.join.group.body", requestUserName, groupName);
        String body1 = MessageHelper.getMessageWithCode("email.request.join.group.body1");
        String body2 = MessageHelper.getMessage("email.request.join.group.body2", "{{{member.card}}}");
        String body3 = MessageHelper.getMessageWithCode("email.request.join.group.body3");
        return greeting.concat("<br>").concat(body).concat("<br>").concat(body1).concat("<br>").concat(body2).concat("<br>").concat(body3);
    }

    private String buildPromotedMemberInGroupTitle(String groupName, String groupRole) {
        return MessageHelper.getMessage("notification.promoted.group.title", groupRole, groupName);
    }

    private String buildPromotedMemberInGroupContent(String groupName, String referenceUrl, String groupRole) {
        String greeting = MessageHelper.getMessage("email.promoted.group.greeting", "{member}", null);
        String body = MessageHelper.getMessage("email.promoted.group.body", groupRole, groupName);
        String body1 = MessageHelper.getMessageWithCode("email.promoted.group.body1");
        String body2 = MessageHelper.getMessage("email.promoted.group.body2", referenceUrl, null);
        return greeting.concat("<br>").concat(body).concat("<br>").concat(body1).concat("<br>").concat(body2);
    }

    private String buildRequestJoinGroupAcceptedTitle(String groupName) {
        return MessageHelper.getMessage("notification.join.group.accepted.title", groupName, null);
    }

    private String buildRequestJoinGroupAcceptedContent(String groupName, String referenceUrl) {
        String greeting = MessageHelper.getMessage("email.join.group.accepted.greeting", "{member}", null);
        String body = MessageHelper.getMessage("email.join.group.accepted.body", groupName, null);
        String body1 = MessageHelper.getMessageWithCode("email.join.group.accepted.body1");
        String body2 = MessageHelper.getMessage("email.join.group.accepted.body2", referenceUrl, null);
        return greeting.concat("<br>").concat(body).concat("<br>").concat(body1).concat("<br>").concat(body2);
    }

    private String buildRequestJoinGroupRejectedTitle(String groupName) {
        return MessageHelper.getMessage("notification.join.group.rejected.title", groupName, null);
    }

    private String buildRequestJoinGroupRejectedContent(String groupName) {
        String greeting = MessageHelper.getMessage("email.join.group.rejected.greeting", "{member}", null);
        String body = MessageHelper.getMessage("email.join.group.rejected.body", groupName, null);
        return greeting.concat("<br>").concat(body);
    }

    private String buildActivityPostGroupTitle(String postAuthor, String groupName) {
        return MessageHelper.getMessage("notification.activity.post.group.title", postAuthor, groupName);
    }

    private String buildActivityPostGroupTitleContent(String postAuthor, String groupName, String referenceUrl, String postContent) {
        String greeting = MessageHelper.getMessage("email.activity.post.group.greeting", "{member}", null);
        String body = MessageHelper.getMessage("email.activity.post.group.body", postAuthor, groupName);
        String body1 = MessageHelper.getMessage("email.activity.post.group.body1", postContent, null);
        String body2 = MessageHelper.getMessageWithCode("email.activity.post.group.body2");
        String body3 = MessageHelper.getMessage("email.activity.post.group.body3", referenceUrl, null);
        return greeting.concat("<br>").concat(body).concat("<br>").concat(body1).concat("<br>").concat(body2).concat("<br>").concat(body3);
    }

    private String buildUpdateManageGroupTitle(String groupName) {
        return MessageHelper.getMessage("notification.update.manage.group.title", groupName, null);
    }

    private String buildUpdateManageGroupContent(String groupName, String groupDescription, String referenceUrl) {
        String greeting = MessageHelper.getMessage("email.update.manage.group.greeting", "{member}", null);
        String body = MessageHelper.getMessage("email.update.manage.group.body", groupName, null);
        String body1 = MessageHelper.getMessage("email.update.manage.group.body1", groupDescription, null);
        String body2 = MessageHelper.getMessageWithCode("email.update.manage.group.body2");
        String body3 = MessageHelper.getMessage("email.update.manage.group.body3", referenceUrl, null);
        return greeting.concat("<br>").concat(body).concat("<br>").concat(body1).concat("<br>").concat(body2).concat("<br>").concat(body3);
    }

    private String buildPostNotifyAllTitle(String userPostName) {
        return MessageHelper.getMessage("notification.activity.post.notifyAll.title", userPostName, null);
    }

    private String buildPostNotifyAllContent(String userPostName, String portalName, String referenceUrl) {
        String contentBody1 = MessageHelper.getMessage("email.activity.post.notifyAll.greeting", "{member}", null);
        String contentBody2 = MessageHelper.getMessage("email.activity.post.notifyAll.body", userPostName, portalName);
        String contentBody3 = MessageHelper.getMessage("email.activity.post.notifyAll.body2", referenceUrl, null);
        return contentBody1.concat("<br>").concat(contentBody2).concat("<br>").concat(contentBody3);
    }

    private String buildPostToFollowersTitle(String userPostName, String groupName) {
        return MessageHelper.getMessage("notification.activity.post.following.title", userPostName, groupName);
    }

    private String buildPostToFollowersContent(String userPostName, String groupName, String referenceUrl) {
        String contentBody1 = MessageHelper.getMessage("email.activity.post.following.greeting", "{member}", null);
        String contentBody2 = MessageHelper.getMessage("email.activity.post.following.body", userPostName, groupName);
        String contentBody3 = MessageHelper.getMessage("email.activity.post.following.body2", referenceUrl, null);
        return contentBody1.concat("<br>").concat(contentBody2).concat("<br>").concat(contentBody3);
    }

    private String buildFlagPostContent(String memberName, String portalName, String reason, String referenceUrl) {
        String greeting = MessageHelper.getMessage("email.activity.flag.post.greeting", "{member}", null);
        String body1 = MessageHelper.getMessage("email.activity.flag.post.body", portalName, memberName);
        String body2 = MessageHelper.getMessage("email.activity.flag.post.body2", reason, null);
        String body3 = MessageHelper.getMessage("email.activity.flag.post.body3", referenceUrl, null);
        String body4 = MessageHelper.getMessageWithCode("email.activity.flag.post.body4");
        return greeting.concat("<br>").concat(body1).concat("<br>").concat(body2).concat("<br>").concat(body3).concat("<br>").concat(body4);
    }

    private String buildFlagCommentContent(String memberName, String portalName, String reason, String referenceUrl) {
        String greeting = MessageHelper.getMessage("email.activity.flag.comment.greeting", "{member}", null);
        String body1 = MessageHelper.getMessage("email.activity.flag.comment.body", portalName, memberName);
        String body2 = MessageHelper.getMessage("email.activity.flag.comment.body2", reason, null);
        String body3 = MessageHelper.getMessage("email.activity.flag.comment.body3", referenceUrl, null);
        String body4 = MessageHelper.getMessageWithCode("email.activity.flag.comment.body4");
        return greeting.concat("<br>").concat(body1).concat("<br>").concat(body2).concat("<br>").concat(body3).concat("<br>").concat(body4);
    }

    private String buildReplyCommentTitle(String childCommentName) {
        return MessageHelper.getMessage("notification.reply.comment.notify.title", childCommentName, null);
    }

    private String buildReplyCommentContent(String childCommentName, String parentCommentName, String referenceUrl, String content) {
        String contentBody1 = MessageHelper.getMessage("email.reply.comment.notify.greeting", parentCommentName, null);
        String contentBody2 = MessageHelper.getMessage("email.reply.comment.notify.body", childCommentName, null);
        String contentBody3 = MessageHelper.getMessage("email.reply.comment.notify.body1", content, null);
        String contentBody4 = MessageHelper.getMessage("email.reply.comment.notify.body2", referenceUrl, null);
        return contentBody1.concat("<br>").concat(contentBody2).concat("<br>").concat(contentBody3).concat("<br>").concat(contentBody4);
    }

    private String buildCommentOnPostTitle(String commentUserName) {
        return MessageHelper.getMessage("notification.activity.comment.notify.title", commentUserName, null);
    }

    private String buildCommentOnPostContent(String commentUserName, String postUserName, String referenceUrl, String content) {
        String contentBody1 = MessageHelper.getMessage("email.activity.comment.notify.greeting", postUserName, null);
        String contentBody2 = MessageHelper.getMessage("email.activity.comment.notify.body", commentUserName, null);
        String contentBody3 = MessageHelper.getMessage("email.activity.comment.notify.body1", content, null);
        String contentBody4 = MessageHelper.getMessage("email.activity.comment.notify.body2", referenceUrl, null);
        return contentBody1.concat("<br>").concat(contentBody2).concat("<br>").concat(contentBody3).concat("<br>").concat(contentBody4);
    }

    private String buildRestrictMemberTitle() {
        return MessageHelper.getMessageWithCode("notification.restrict.user.notify.title");
    }

    private String buildRestrictMemberContent(String userName) {
        String contentBody1 = MessageHelper.getMessage("email.restrict.user.notify.greeting", userName, null);
        String contentBody2 = MessageHelper.getMessageWithCode("email.restrict.user.notify.body");
        String contentBody3 = MessageHelper.getMessageWithCode("email.restrict.user.notify.body2");
        return contentBody1.concat("<br>").concat(contentBody2).concat("<br>").concat(contentBody3);
    }

    private String buildMentionMemberInCommentTitle(String commentAuthor) {
        return MessageHelper.getMessage("notification.direct.comment.mention.title", commentAuthor, null);
    }

    private String buildMentionMemberInCommentContent(String commentAuthor, String commentUrl, String commentContent) {
        String contentBody1 = MessageHelper.getMessage("email.direct.comment.mention.greeting", "{member}", null);
        String contentBody2 = MessageHelper.getMessage("email.direct.comment.mention.body", commentAuthor, null);
        String contentBody3 = MessageHelper.getMessage("email.direct.comment.mention.body1", commentContent, null);
        String contentBody4 = MessageHelper.getMessage("email.direct.comment.mention.body2", commentUrl, null);
        return contentBody1.concat("<br>").concat(contentBody2).concat("<br>").concat(contentBody3).concat("<br>").concat(contentBody4);
    }

    private String buildMentionMemberInPostTitle(String postAuthor) {
        return MessageHelper.getMessage("notification.direct.post.mention.title", postAuthor, null);
    }

    private String buildMentionMemberInPostContent(String postAuthor, String postUrl) {
        String contentBody1 = MessageHelper.getMessage("email.direct.post.mention.greeting", "{member}", null);
        String contentBody2 = MessageHelper.getMessage("email.direct.post.mention.body", postAuthor, null);
        String contentBody3 = MessageHelper.getMessage("email.direct.post.mention.body2", postUrl, null);
        return contentBody1.concat("<br>").concat(contentBody2).concat("<br>").concat(contentBody3);
    }
}
