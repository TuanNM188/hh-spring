package com.formos.huub.service.communityboard;

import com.formos.huub.domain.entity.CommunityBoardComment;
import com.formos.huub.domain.entity.CommunityBoardFile;
import com.formos.huub.domain.entity.CommunityBoardPost;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.enums.CommunityBoardEntryTypeEnum;
import com.formos.huub.domain.enums.CommunityBoardVisibilityEnum;
import com.formos.huub.domain.request.communityboardcomment.RequestCreateCommunityBoardComment;
import com.formos.huub.domain.request.communityboardcomment.RequestGetListCommunityBoardComment;
import com.formos.huub.domain.request.communityboardcomment.RequestGetListMemberMention;
import com.formos.huub.domain.request.communityboardcomment.RequestUpdateCommunityBoardComment;
import com.formos.huub.domain.request.communityboardpost.RequestCommunityBoardFile;
import com.formos.huub.domain.request.member.RequestGetUserInPortal;
import com.formos.huub.domain.response.communityboard.IResponseMentionUser;
import com.formos.huub.domain.response.communityboardcomment.ResponseCommunityBoardComment;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.mapper.communityboard.CommunityBoardCommentMapper;
import com.formos.huub.mapper.communityboard.CommunityBoardFileMapper;
import com.formos.huub.mapper.member.MemberMapper;
import com.formos.huub.repository.*;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.pushnotification.PushNotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.*;

import static com.formos.huub.security.SecurityUtils.isAdminOrPortalHostRole;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommunityBoardCommentService extends BaseService {

    CommunityBoardCommentRepository commentRepository;
    CommunityBoardGroupRepository groupRepository;
    UserRepository userRepository;
    CommunityBoardFileRepository fileRepository;
    CommunityBoardPostRepository postRepository;
    MemberRepository memberRepository;
    CommunityBoardCommentMapper communityBoardCommentMapper;
    CommunityBoardFileMapper communityBoardFileMapper;
    MemberMapper memberMapper;
    PushNotificationService pushNotificationService;

    public Map<String, Object> getCommentForPost(RequestGetListCommunityBoardComment request, Pageable pageable) {
        UUID postId = UUIDUtils.convertToUUID(request.getPostId());
        UUID parentId = UUIDUtils.toUUID(request.getParentId());
        List<UUID> ignoreCommentIds = UUIDUtils.toUUIDs(StringUtils.convertStringToListString(request.getIgnoreCommentIds()));
        var user = SecurityUtils.getCurrentUser(userRepository);
        var pageCommentIds = commentRepository.getCommentIdsForPost(postId, parentId, ignoreCommentIds, pageable);
        var result = PageUtils.toPage(pageCommentIds);
        var iComments = commentRepository.getDetailCommentFromIds(pageCommentIds.getContent(), user.getId());
        result.put("content", iComments);
        return result;
    }

    public void updateComment(RequestUpdateCommunityBoardComment request, UUID commentId) {
        var user = SecurityUtils.getCurrentUser(userRepository);
        var comment = validateAndRetrieveComment(commentId, user);
        var post = getPostById(comment.getPostId());

        comment = communityBoardCommentMapper.partialUpdate(comment, request);
        commentRepository.save(comment);

        handleCommentFiles(request.getFiles(), commentId, user.getId());

        if (!CollectionUtils.isEmpty(request.getMentionUserIds())) {
            log.info("Sending mention comment notification on comment: {}", comment.getId());
            var files = fileRepository.findAllByEntryTypeAndEntryId(CommunityBoardEntryTypeEnum.COMMENT, commentId);
            pushNotificationService.sendMentionNotificationForComment(request.getMentionUserIds(), comment, files, post);
        }
    }

    public void deleteComment(UUID commentId) {
        var user = SecurityUtils.getCurrentUser(userRepository);
        var comment = getCommentById(commentId);
        var isManager = isAdminOrPortalHostRole();
        if (!comment.getAuthorId().equals(user.getId()) && !isManager) {
            throw new AccessDeniedException("Role Member");
        }
        commentRepository.deleteCommentTree(commentId);
    }

    public ResponseCommunityBoardComment createComment(RequestCreateCommunityBoardComment request) {
        var user = SecurityUtils.getCurrentUser(userRepository);

        var post = getPostById(UUIDUtils.convertToUUID(request.getPostId()));
        var commentEntity = communityBoardCommentMapper.toEntity(request);
        commentEntity.setAuthorId(user.getId());
        commentRepository.save(commentEntity);

        var response = buildCommentResponse(commentEntity, user);
        var fileEntities = handleFileAttachments(request.getFiles(), commentEntity.getId(), user.getId());
        response.setFiles(communityBoardFileMapper.toResponses(fileEntities));

        if (!CollectionUtils.isEmpty(request.getMentionUserIds())) {
            log.info("Sending mention comment notification on comment: {}", commentEntity.getId());
            pushNotificationService.sendMentionNotificationForComment(request.getMentionUserIds(), commentEntity, fileEntities, post);
        }
        sendParentOrPostNotification(commentEntity, fileEntities, post);

        if (CommunityBoardVisibilityEnum.GROUP.equals(post.getVisibility())) {
            groupRepository.updateLastActive(post.getGroupId(), Instant.now());
        }

        return response;
    }

    public List<IResponseMentionUser> getListMemberMention(RequestGetListMemberMention request) {
        var post = getPostById(UUIDUtils.convertToUUID(request.getPostId()));
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        var requestSearch = RequestGetUserInPortal.builder()
            .portalId(post.getPortalId())
            .visibility(post.getVisibility().getValue())
            .groupId(post.getGroupId())
            .postAuthorId(post.getAuthorId())
            .ignoreUserId(currentUser.getId())
            .build();
        return memberRepository.getAllUserInPortalByCondition(requestSearch);
    }

    private void sendParentOrPostNotification(CommunityBoardComment commentEntity, List<CommunityBoardFile> files, CommunityBoardPost post) {
        if (Objects.nonNull(commentEntity.getParentId())) {
            var parentUser = commentRepository.findAuthorUser(commentEntity.getParentId())
                .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "User Comment")));
            if (parentUser.getId().equals(commentEntity.getAuthorId())) {
                return;
            }
            log.info("Sending reply comment notification to user: {}", parentUser.getId());
            pushNotificationService.sendNotifyWhenMemberReplyComment(commentEntity, parentUser, files, post);
        } else {
            var postUser = postRepository.findAuthorUser(commentEntity.getPostId())
                .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "User Comment")));
            if (postUser.getId().equals(commentEntity.getAuthorId())) {
                return;
            }
            log.info("Sending comment post notification to user: {}", postUser.getId());
            pushNotificationService.sendNotifyWhenMemberCommentOnPost(commentEntity, postUser, files, post);
        }
    }

    private ResponseCommunityBoardComment buildCommentResponse(
        CommunityBoardComment commentEntity,
        User user
    ) {
        var response = communityBoardCommentMapper.toResponse(commentEntity);
        response.setAuthor(memberMapper.toResponseCommunityBoard(user));
        return response;
    }


    private List<CommunityBoardFile> handleFileAttachments(List<RequestCommunityBoardFile> files, UUID commentId, UUID ownerId) {
        if (CollectionUtils.isEmpty(files)) {
            return new ArrayList<>();
        }
        var fileEntities = files.stream()
            .map(f -> communityBoardFileMapper.toEntity(f, CommunityBoardEntryTypeEnum.COMMENT, commentId, ownerId))
            .toList();

        fileRepository.saveAll(fileEntities);
        return fileEntities;
    }


    private CommunityBoardComment validateAndRetrieveComment(UUID commentId, User user) {
        var comment = getCommentById(commentId);
        var isManager = isAdminOrPortalHostRole();

        if (!comment.getAuthorId().equals(user.getId()) && !isManager) {
            throw new AccessDeniedException("Role Member");
        }

        return comment;
    }

    private void handleCommentFiles(List<RequestCommunityBoardFile> files, UUID commentId, UUID ownerId) {
        var oldFiles = fileRepository.findAllByEntryTypeAndEntryId(CommunityBoardEntryTypeEnum.COMMENT, commentId);

        if (CollectionUtils.isEmpty(files)) {
            fileRepository.deleteAll(oldFiles);
            return;
        }

        var currentFiles = files.stream().filter(f -> Objects.nonNull(f.getId())).toList();
        var filesToRemove = oldFiles.stream()
            .filter(f -> currentFiles.stream().noneMatch(t -> UUIDUtils.convertToUUID(t.getId()).equals(f.getId())))
            .toList();

        var newFiles = files.stream()
            .filter(f -> Objects.isNull(f.getId()))
            .map(f -> communityBoardFileMapper.toEntity(f, CommunityBoardEntryTypeEnum.COMMENT, commentId, ownerId))
            .toList();

        fileRepository.deleteAll(filesToRemove);
        fileRepository.saveAll(newFiles);
    }

    private CommunityBoardPost getPostById(UUID id) {
        return postRepository
                .findById(id)
                .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Post")));
    }

    private CommunityBoardComment getCommentById(UUID id) {
        return commentRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Comment")));
    }

}
