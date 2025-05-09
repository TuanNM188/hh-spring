package com.formos.huub.service.communityboard;

import com.formos.huub.domain.entity.CommunityBoardFile;
import com.formos.huub.domain.entity.CommunityBoardPost;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.enums.*;
import com.formos.huub.domain.request.communityboardpost.*;
import com.formos.huub.domain.request.member.RequestGetUserInPortal;
import com.formos.huub.domain.response.communityboard.IResponseMentionUser;
import com.formos.huub.domain.response.communityboardcomment.ResponseCommunityBoardCommentDetail;
import com.formos.huub.domain.response.communityboardpost.IResponseCommunityBoardPost;
import com.formos.huub.domain.response.communityboardpost.ResponseCommunityBoardPost;
import com.formos.huub.domain.response.communityboardpost.ResponseCommunityBoardPostDetail;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.constant.AppConstants;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.mapper.communityboard.CommunityBoardCommentMapper;
import com.formos.huub.mapper.communityboard.CommunityBoardFileMapper;
import com.formos.huub.mapper.communityboard.CommunityBoardPostMapper;
import com.formos.huub.mapper.member.MemberMapper;
import com.formos.huub.repository.*;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.pushnotification.PushNotificationService;
import com.formos.huub.service.quartz.CreateQuartzJobService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.formos.huub.security.SecurityUtils.isAdminOrPortalHostRole;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommunityBoardPostService extends BaseService {

    CommunityBoardPostRepository postRepository;
    CommunityBoardGroupRepository groupRepository;
    CommunityBoardGroupMemberRepository groupMemberRepository;
    CommunityBoardGroupSettingRepository groupSettingRepository;
    CommunityBoardCommentRepository commentRepository;
    CommunityBoardFileRepository fileRepository;
    CommunityBoardUserRestrictionRepository userRestrictionRepository;
    MemberRepository memberRepository;
    UserRepository userRepository;
    CreateQuartzJobService createQuartzJobService;
    CommunityBoardPostMapper communityBoardPostMapper;
    CommunityBoardFileMapper communityBoardFileMapper;
    CommunityBoardCommentMapper communityBoardCommentMapper;
    MemberMapper memberMapper;
    PushNotificationService pushNotificationService;
    FollowRepository followRepository;

    public Map<String, Object> getPostForMember(RequestGetListCommunityBoardPost request, Pageable pageable) {
        if (Objects.isNull(request.getPortalId())) {
            request.setPortalId(PortalContextHolder.getPortalId());
        }

        var user = SecurityUtils.getCurrentUser(userRepository);
        request.setUserId(user.getId());

        request.setSearchKeyword(StringUtils.makeStringWithContain(request.getSearchKeyword()));

        Page<UUID> pagePostIds = getPagePostIds(request, user.getId(), pageable);

        var result = PageUtils.toPage(pagePostIds);
        var iPosts = postRepository.getDetailPostFromIds(pagePostIds.getContent(), user.getId());
        result.put("content", iPosts);

        return result;
    }

    private Page<UUID> getPagePostIds(RequestGetListCommunityBoardPost request, UUID userId, Pageable pageable) {
        if (Objects.isNull(request.getGroupId())) {
            return postRepository.getPostIdsForDashboard(request, pageable);
        }
        if (!validateGetPostInGroup(request.getGroupId(), userId)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0057));
        }
        return postRepository.getPostIdsForGroup(request, pageable);
    }

    public Map<String, Object> getPostsMentionTab(RequestGetListCommunityBoardPost request, Pageable pageable) {
        if (Objects.isNull(request.getPortalId())) {
            request.setPortalId(PortalContextHolder.getPortalId());
        }
        var user = SecurityUtils.getCurrentUser(userRepository);
        request.setUserId(user.getId());
        request.setSearchKeyword(StringUtils.makeStringWithContain(request.getSearchKeyword()));
        request.setMentionUser(StringUtils.makeStringWithContain(user.getId().toString()));
        Page<UUID> pagePostIds = postRepository.getPostIdsForMentionTab(request, pageable);
        var result = PageUtils.toPage(pagePostIds);

        var iPosts = postRepository.getDetailPostFromIds(pagePostIds.getContent(), user.getId());
        result.put("content", iPosts);
        return result;
    }

    public Map<String, Object> getPostsFollowingTab(RequestGetListCommunityBoardPost request, Pageable pageable) {
        if (Objects.isNull(request.getPortalId())) {
            request.setPortalId(PortalContextHolder.getPortalId());
        }
        var user = SecurityUtils.getCurrentUser(userRepository);
        request.setUserId(user.getId());
        request.setSearchKeyword(StringUtils.makeStringWithContain(request.getSearchKeyword()));
        Page<UUID> pagePostIds = postRepository.getPostIdsForFollowingTab(request, pageable);
        var result = PageUtils.toPage(pagePostIds);

        var iPosts = postRepository.getDetailPostFromIds(pagePostIds.getContent(), user.getId());
        result.put("content", iPosts);
        return result;
    }

    private boolean validateGetPostInGroup(UUID groupId, UUID userId) {
        var groupPrivacy = groupSettingRepository.findByGroupIdAndSettingKey(groupId, SettingKeyCodeEnum.PRIVACY_OPTIONS)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0020, "Group")));

        if (PrivacyOptionSettingValueEnum.PUBLIC_GROUPS.getValue().equals(groupPrivacy)) {
            return true;
        }
        var groupRoles = List.of(CommunityBoardGroupRoleEnum.MEMBER, CommunityBoardGroupRoleEnum.MODERATOR, CommunityBoardGroupRoleEnum.ORGANIZER);
        return groupMemberRepository.existsMemberInGroup(userId, groupId, groupRoles);
    }

    public ResponseCommunityBoardPostDetail getDetailPost(UUID postId, String highlightComment) {
        var user = SecurityUtils.getCurrentUser(userRepository);
        var result = communityBoardPostMapper.toResponse(getDetailPost(postId, user.getId()));

        if (StringUtils.isBlank(highlightComment)) {
            return result;
        }
        var highlightCommentId = UUIDUtils.convertToUUID(highlightComment);
        result.setHighlightComment(getDetailHighlightComment(highlightCommentId, user.getId(), null));
        return result;
    }

    private ResponseCommunityBoardCommentDetail getDetailHighlightComment(UUID commentId, UUID userId, ResponseCommunityBoardCommentDetail highlightComment) {
        var iComments = commentRepository.getDetailCommentFromIds(List.of(commentId), userId);
        if (CollectionUtils.isEmpty(iComments)) {
            return highlightComment;
        }
        var result = communityBoardCommentMapper.toResponse(iComments.getFirst());
        result.setHighlightComment(highlightComment);
        if (Objects.isNull(result.getParentId())) {
            return result;
        }
        return getDetailHighlightComment(result.getParentId(), userId, result);
    }

    private IResponseCommunityBoardPost getDetailPost(UUID postId, UUID userId) {
        var iPosts = postRepository.getDetailPostFromIds(List.of(postId), userId);
        if (CollectionUtils.isEmpty(iPosts)) {
            return null;
        }
        return iPosts.getFirst();
    }

    public IResponseCommunityBoardPost updatePost(RequestUpdateCommunityBoardPost request, UUID postId) {
        var user = SecurityUtils.getCurrentUser(userRepository);
        var post = getPostById(postId);
        var portalId = resolvePortalId(request.getPortalId());
        validateUpdatePermissions(user, post, portalId, request.getFiles());

        post = communityBoardPostMapper.partialUpdate(post, request);
        post.setPortalId(portalId);
        postRepository.save(post);

        handleFileUpdates(postId, request.getFiles(), user.getId());

        if (post.getScheduledTime().isAfter(Instant.now())) {
            createQuartzJobService.createJobPublicPost(request.getMentionUserIds(), post);
            return getDetailPost(post.getId(), user.getId());
        }

        if (!CollectionUtils.isEmpty(request.getMentionUserIds())) {
            log.info("Sending mention notification to users by post: {}", post.getId());
            var files = fileRepository.findAllByEntryTypeAndEntryId(CommunityBoardEntryTypeEnum.POST, postId);
            pushNotificationService.sendMentionNotificationForPost(UUIDUtils.toUUIDs(request.getMentionUserIds()), post, files);
        }

        return getDetailPost(post.getId(), user.getId());
    }

    public void deletePost(UUID postId) {
        var user = SecurityUtils.getCurrentUser(userRepository);
        var post = getPostById(postId);
        boolean isManagerGroup = false;
        if (CommunityBoardVisibilityEnum.GROUP.equals(post.getVisibility())) {
            isManagerGroup = isManagerGroup(post.getGroupId(), user.getId());
        }
        var isManager = isAdminOrPortalHostRole();
        if (!post.getAuthorId().equals(user.getId()) && !isManager && !isManagerGroup) {
            throw new AccessDeniedException("Role Member");
        }

        if (post.getScheduledTime().isAfter(Instant.now())) {
            createQuartzJobService.deleteJobPublicPost(postId);
        }
        postRepository.delete(post);
    }

    private boolean isManagerGroup(UUID groupId, UUID currentUserId) {
        var managerRole = List.of(CommunityBoardGroupRoleEnum.ORGANIZER, CommunityBoardGroupRoleEnum.MODERATOR);
        return groupMemberRepository.findMembersByGroupRoles(groupId, managerRole)
            .stream().anyMatch(m -> currentUserId.equals(m.getId()));
    }

    public ResponseCommunityBoardPost createPost(RequestCreateCommunityBoardPost request) {
        var user = SecurityUtils.getCurrentUser(userRepository);
        var portalId = resolvePortalId(request.getPortalId());
        List<UUID> ignoreMemberIds = new ArrayList<>();
        ignoreMemberIds.add(user.getId());

        List<UUID> portalIds = determinePortalIds(request, user, portalId);
        var postEntities = buildPostEntity(request, portalIds, user);

        var postEntity = findPostEntityByPortalId(postEntities, portalId);

        if (CommunityBoardVisibilityEnum.GROUP.equals(postEntity.getVisibility())) {
            validateMemberPostInGroup(user.getId(), postEntity.getGroupId());
        }

        adjustVisibilityForGroupPosts(postEntities, postEntity);

        postRepository.saveAll(postEntities);
        var postIds = postEntities.stream().map(CommunityBoardPost::getId).toList();

        var listFile = handleFileAttachments(request.getFiles(), postIds, postEntity.getId(), user.getId());
        if (postEntity.getScheduledTime().isAfter(Instant.now())) {
            postEntities.forEach(post -> createQuartzJobService.createJobPublicPost(request.getMentionUserIds(), post));
            return buildResponse(postEntity, user, listFile);
        }
        var mentionUserIds = UUIDUtils.toUUIDs(request.getMentionUserIds());
        handleMentionNotifications(mentionUserIds, postEntities, listFile);
        ignoreMemberIds.addAll(mentionUserIds);
        String groupName = AppConstants.COMMUNITY_BOARD;
        if (Objects.nonNull(postEntity.getGroupId())) {
            groupName = groupRepository.findGroupNameById(postEntity.getGroupId()).orElse(AppConstants.COMMUNITY_BOARD);
        }
        var followerIds = followRepository.findAllByFollowedId(user.getId(), ignoreMemberIds);
        handleFollowersNotifications(followerIds, postEntities, user.getNormalizedFullName(), groupName);
        ignoreMemberIds.addAll(followerIds);

        if (CommunityBoardVisibilityEnum.GROUP.equals(postEntity.getVisibility())) {
            groupRepository.updateLastActive(postEntity.getGroupId(), Instant.now());

            var memberIds = groupMemberRepository.findMembersByGroupRoles(postEntity.getGroupId(), ignoreMemberIds, null);

            pushNotificationService.sendNotificationPostInGroup(groupName, postEntity, user.getNormalizedFullName(), memberIds);

        } else if (Boolean.TRUE.equals(postEntity.getIsNotifyAll()) && isAdminOrPortalHostRole()) {
            // notify all for admin and portal host
            postEntities.forEach(post -> notifyAllUsersForPost(post, user, ignoreMemberIds));
        }
        return buildResponse(postEntity, user, listFile);
    }

    private void validateMemberPostInGroup(UUID userId, UUID groupId) {
        var settingGroup = groupSettingRepository.findByGroupIdAndSettingKey(groupId, SettingKeyCodeEnum.GROUP_POSTS)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0020, "Group")));

        var groupMember = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0073)));

        if (isOrganizer(groupMember.getGroupRole())) {
            return;
        }
        if (isModeratorWithValidSetting(groupMember.getGroupRole(), settingGroup)) {
            return;
        }
        if (isMemberWithValidSetting(groupMember.getGroupRole(), settingGroup)) {
            return;
        }

        throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0073));
    }

    private boolean isOrganizer(CommunityBoardGroupRoleEnum groupRole) {
        return CommunityBoardGroupRoleEnum.ORGANIZER.equals(groupRole);
    }

    private boolean isModeratorWithValidSetting(CommunityBoardGroupRoleEnum groupRole, String settingGroup) {
        return CommunityBoardGroupRoleEnum.MODERATOR.equals(groupRole)
            && !CommunityBoardSettingValueEnum.ORGANIZERS_ONLY.getValue().equals(settingGroup);
    }

    private boolean isMemberWithValidSetting(CommunityBoardGroupRoleEnum groupRole, String settingGroup) {
        return CommunityBoardSettingValueEnum.ALL_GROUP_MEMBERS.getValue().equals(settingGroup)
            && CommunityBoardGroupRoleEnum.MEMBER.equals(groupRole);
    }

    public void publicPostWhenScheduleTime(List<UUID> mentionUserIds, UUID postId) {
        var post = getPostById(postId);
        List<UUID> ignoreMemberIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(mentionUserIds)) {
            log.info("Sending mention notification to users by post: {}", post.getId());
            var files = fileRepository.findAllByEntryTypeAndEntryId(CommunityBoardEntryTypeEnum.POST, postId);
            ignoreMemberIds.addAll(mentionUserIds);
            pushNotificationService.sendMentionNotificationForPost(mentionUserIds, post, files);
        }

        if (Boolean.TRUE.equals(post.getIsNotifyAll())) {
            var authorPost = userRepository.findById(post.getAuthorId())
                .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "User")));
            ignoreMemberIds.add(authorPost.getId());
            notifyAllUsersForPost(post, authorPost, ignoreMemberIds);
        }
    }

    public List<IResponseMentionUser> getListMemberMention(RequestGetListMemberMention request) {
        var user = SecurityUtils.getCurrentUser(userRepository);
        List<UUID> portalIds = new ArrayList<>();
        if (CollectionUtils.isEmpty(request.getPortalIds())) {
            portalIds.add(PortalContextHolder.getPortalId());
        } else {
            portalIds.addAll(UUIDUtils.toUUIDs(request.getPortalIds()));
        }
        var visibility = CommunityBoardVisibilityEnum.valueOf(request.getVisibility());
        var groupId = UUIDUtils.toUUID(request.getGroupId());
        var requestSearch = RequestGetUserInPortal.builder()
            .portalIds(portalIds)
            .visibility(visibility.getValue())
            .groupId(groupId)
            .postAuthorId(user.getId())
            .ignoreUserId(user.getId())
            .build();
        return memberRepository.getAllUserInPortalByCondition(requestSearch);
    }

    public void createPinPost(UUID postId) {
        var post = getPostById(postId);
        if (!CommunityBoardVisibilityEnum.ALL_MEMBERS.equals(post.getVisibility())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0059));
        }
        var postOpt = postRepository.findByIsPinAndPortalId(true, post.getPortalId());
        if (postOpt.isPresent()) {
            var postWasPin = postOpt.get();
            postWasPin.setIsPin(false);
            postRepository.save(postWasPin);
        }
        post.setIsPin(true);
        postRepository.save(post);
    }

    public void removePinPost(UUID postId) {
        var post = getPostById(postId);
        post.setIsPin(false);
        postRepository.save(post);
    }

    public Integer getTotalComment(UUID postId) {
        return commentRepository.countByPostId(postId);
    }

    private List<UUID> determinePortalIds(RequestCreateCommunityBoardPost request, User user, UUID portalId) {
        if (Boolean.parseBoolean(request.getIsNotifyAll()) && isAdminOrPortalHostRole()) {
            return UUIDUtils.toUUIDs(request.getPortalIds());
        }

        validateCreatePostRequest(portalId, user, request.getFiles());
        return List.of(portalId);
    }

    private CommunityBoardPost findPostEntityByPortalId(List<CommunityBoardPost> postEntities, UUID portalId) {
        if (postEntities.isEmpty()) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0057));
        }
        return postEntities.stream()
            .filter(post -> post.getPortalId().equals(portalId))
            .findFirst()
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0057)));
    }

    private void adjustVisibilityForGroupPosts(List<CommunityBoardPost> postEntities, CommunityBoardPost mainPostEntity) {
        if (!CommunityBoardVisibilityEnum.GROUP.equals(mainPostEntity.getVisibility())) {
            return;
        }
        postEntities.stream()
            .filter(post -> !post.getPortalId().equals(mainPostEntity.getPortalId()))
            .forEach(post -> {
                post.setVisibility(CommunityBoardVisibilityEnum.ALL_MEMBERS);
                post.setGroupId(null);
            });
    }

    private void handleMentionNotifications(List<UUID> mentionUserIds, List<CommunityBoardPost> postEntities, List<CommunityBoardFile> listFile) {
        if (CollectionUtils.isEmpty(mentionUserIds)) {
            return;
        }
        log.info("Sending mention notification to users by new post");
        pushNotificationService.sendMentionNotificationForListPost(mentionUserIds, postEntities, listFile);
    }

    private void handleFollowersNotifications(List<UUID> followerIds, List<CommunityBoardPost> postEntities, String postAuthor, String groupName) {
        if (CollectionUtils.isEmpty(followerIds)) {
            return;
        }
        log.info("Sending followers notification to users by new post");
        pushNotificationService.sendPostNotificationToFollowers(postEntities, followerIds, postAuthor, groupName);
    }

    private void validateCreatePostRequest(UUID portalId, User user, List<RequestCommunityBoardFile> files) {
        if (Objects.isNull(portalId)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0020, "Portal"));
        }

        boolean isRestricted = userRestrictionRepository.existsByPortalIdAndUserId(portalId, user.getId(), CommunityBoardRestrictionTypeEnum.POST);
        if (isRestricted) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0063));
        }
        validateFileLimits(files);
    }

    private List<CommunityBoardPost> buildPostEntity(RequestCreateCommunityBoardPost request, List<UUID> portalIds, User user) {
        if (Objects.isNull(request.getScheduledTime())) {
            request.setScheduledTime(Instant.now());
        } else if (request.getScheduledTime().isBefore(Instant.now())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0084));
        }
        return portalIds.stream().map(portalId -> communityBoardPostMapper.toEntity(request, portalId, user.getId())).toList();
    }

    private List<CommunityBoardFile> handleFileAttachments(List<RequestCommunityBoardFile> files, List<UUID> postIds, UUID postCurrentPortal, UUID ownerId) {
        if (CollectionUtils.isEmpty(files)) {
            return new ArrayList<>();
        }

        List<CommunityBoardFile> allFiles = new ArrayList<>();
        List<CommunityBoardFile> filesCurrentPortal = new ArrayList<>();

        postIds.forEach(postId -> files
            .forEach(file -> {
                var fileEntity = communityBoardFileMapper.toEntity(file, CommunityBoardEntryTypeEnum.POST, postId, ownerId);
                allFiles.add(fileEntity);

                if (postId.equals(postCurrentPortal)) {
                    filesCurrentPortal.add(fileEntity);
                }
            }));

        fileRepository.saveAll(allFiles);

        return filesCurrentPortal;
    }

    private ResponseCommunityBoardPost buildResponse(CommunityBoardPost postEntity, User user, List<CommunityBoardFile> files) {
        var response = communityBoardPostMapper.toResponse(postEntity);
        if (Objects.nonNull(postEntity.getGroupId())) {
            response.setGroupName(groupRepository.findGroupNameById(postEntity.getGroupId()).orElse(AppConstants.KEY_EMPTY));
        }
        response.setAuthor(memberMapper.toResponseCommunityBoard(user));
        response.setFiles(communityBoardFileMapper.toResponses(files));
        return response;
    }

    private UUID resolvePortalId(String portalId) {
        var resolvedPortalId = UUIDUtils.toUUID(portalId);
        if (Objects.isNull(resolvedPortalId)) {
            return PortalContextHolder.getPortalId();
        }
        return resolvedPortalId;
    }

    private void validateUpdatePermissions(User user, CommunityBoardPost post, UUID portalId, List<RequestCommunityBoardFile> files) {
        boolean isManagerGroup = false;
        if (CommunityBoardVisibilityEnum.GROUP.equals(post.getVisibility())) {
            isManagerGroup = isManagerGroup(post.getGroupId(), user.getId());
        }
        boolean isManager = isAdminOrPortalHostRole();
        if (!post.getAuthorId().equals(user.getId()) && !isManager && !isManagerGroup) {
            throw new AccessDeniedException("Role Member");
        }
        if (!post.getPortalId().equals(portalId)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0020, "Portal"));
        }
        validateFileLimits(files);
    }

    private void validateFileLimits(List<RequestCommunityBoardFile> files) {
        if (CollectionUtils.isEmpty(files)) {
            return;
        }

        Map<String, Long> fileTypeCounts = files.stream()
            .collect(Collectors.groupingBy(
                RequestCommunityBoardFile::getMediaType,
                Collectors.counting()
            ));

        long totalVideo = fileTypeCounts.getOrDefault(CommunityBoardFileTypeEnum.VIDEO.getValue(), 0L);
        long totalImageAndFile = files.size() - totalVideo;

        if (totalImageAndFile > AppConstants.LIMIT_IMAGE_IN_POST || totalVideo > AppConstants.LIMIT_VIDEO_IN_POST) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0062));
        }
    }

    private void handleFileUpdates(UUID postId, List<RequestCommunityBoardFile> files, UUID ownerId) {
        var oldFiles = fileRepository.findAllByEntryTypeAndEntryId(CommunityBoardEntryTypeEnum.POST, postId);

        if (CollectionUtils.isEmpty(files)) {
            fileRepository.deleteAll(oldFiles);
            return;
        }
        var currentFiles = files.stream().filter(f -> Objects.nonNull(f.getId())).toList();
        var removeFiles = oldFiles
            .stream()
            .filter(f -> currentFiles.stream().noneMatch(t -> UUIDUtils.convertToUUID(t.getId()).equals(f.getId())))
            .toList();
        var newFiles = files
            .stream()
            .filter(f -> Objects.isNull(f.getId()))
            .map(f -> communityBoardFileMapper.toEntity(f, CommunityBoardEntryTypeEnum.POST, postId, ownerId))
            .toList();
        fileRepository.deleteAll(removeFiles);
        fileRepository.saveAll(newFiles);

    }

    private void notifyAllUsersForPost(CommunityBoardPost post, User authorPost, List<UUID> ignoreMemberIds) {
        var userIds = memberRepository.getAllUserIdCanViewPost(
            post.getPortalId(),
            post.getVisibility().getValue(),
            post.getGroupId(),
            post.getAuthorId(),
            ignoreMemberIds
        );
        log.info("Sending notify all notification to user by post: {}", post.getId());
        pushNotificationService.sendNotifyAllForPostFromAdmin(post, authorPost.getNormalizedFullName(), userIds);
    }

    private CommunityBoardPost getPostById(UUID id) {
        return postRepository.findById(id).orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Post")));
    }

}
