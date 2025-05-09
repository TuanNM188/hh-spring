package com.formos.huub.service.communityboard;

import com.formos.huub.domain.entity.CommunityBoardFlag;
import com.formos.huub.domain.entity.CommunityBoardPost;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.enums.CommunityBoardGroupRoleEnum;
import com.formos.huub.domain.enums.CommunityBoardTargetTypeEnum;
import com.formos.huub.domain.enums.CommunityBoardVisibilityEnum;
import com.formos.huub.domain.request.communityboardpost.RequestMemberFlagContent;
import com.formos.huub.domain.response.communityboardflag.IResponseCommunityBoardFlag;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.mapper.communityboard.CommunityBoardFlagMapper;
import com.formos.huub.repository.*;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.pushnotification.PushNotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommunityBoardFlagService extends BaseService {

    CommunityBoardPostRepository postRepository;
    CommunityBoardFlagRepository flagRepository;
    CommunityBoardGroupMemberRepository groupMemberRepository;
    MemberRepository memberRepository;
    UserRepository userRepository;
    CommunityBoardFlagMapper communityBoardFlagMapper;
    PushNotificationService pushNotificationService;

    public void createFlagPost(RequestMemberFlagContent request) {
        var flag = communityBoardFlagMapper.toEntity(request);
        var currentUser = getCurrentUser();
        flag.setPerformedId(currentUser.getId());
        flag.setPerformedAt(Instant.now());
        if (CommunityBoardTargetTypeEnum.POST.equals(flag.getTargetType())) {
            sendNotifyFlagPost(flag);
        } else {
            sendNotifyFlagComment(flag);
        }
    }

    public IResponseCommunityBoardFlag getDetailFlag(UUID flagId) {
        var flag = getFlagById(flagId);
        if (CommunityBoardTargetTypeEnum.POST.equals(flag.getTargetType())) {
            return flagRepository
                .getDetailFlagPost(flag.getId(), flag.getTargetId())
                .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0066)));
        }
        return flagRepository
            .getDetailFlagComment(flag.getId(), flag.getTargetId())
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0067)));
    }

    private void sendNotifyFlagPost(CommunityBoardFlag flag) {
        var post = getPostById(flag.getTargetId());
        flag.setPortalId(post.getPortalId());
        flagRepository.save(flag);
        var portalInfo = pushNotificationService.getPortalInfo(post.getPortalId());

        if (CommunityBoardVisibilityEnum.GROUP.equals(post.getVisibility())) {
            var managerGroupIds = groupMemberRepository.findMembersByGroupRoles(post.getGroupId(), List.of(CommunityBoardGroupRoleEnum.ORGANIZER))
                .stream().map(User::getId).toList();
            if (!CollectionUtils.isEmpty(managerGroupIds)) {
                log.info("Sending flag post notification to manager Group by post: {}", flag.getTargetId());
                pushNotificationService.sendNotifyFlagPostForManager(flag.getId(), flag.getReason(), managerGroupIds, portalInfo, post.getPortalId());
            }
            return;
        }

        var portalHostIds = memberRepository.getAllPortalHostByPortalId(post.getPortalId());
        var adminIds = memberRepository.getAllSystemAdminId();

        // send notify for manager
        if (!CollectionUtils.isEmpty(portalHostIds)) {
            log.info("Sending flag post notification to portal host by post: {}", flag.getTargetId());
            pushNotificationService.sendNotifyFlagPostForManager(flag.getId(), flag.getReason(), portalHostIds, portalInfo, post.getPortalId());
        }
        if (!CollectionUtils.isEmpty(adminIds)) {
            log.info("Sending flag post notification to admin by post: {}", flag.getTargetId());
            portalInfo = pushNotificationService.getPortalInfoForAdmin();
            pushNotificationService.sendNotifyFlagPostForManager(flag.getId(), flag.getReason(), adminIds, portalInfo, post.getPortalId());
        }
    }

    private void sendNotifyFlagComment(CommunityBoardFlag flag) {
        var post = getPostByCommentId(flag.getTargetId());
        flag.setPortalId(post.getPortalId());
        flagRepository.save(flag);
        var portalInfo = pushNotificationService.getPortalInfo(post.getPortalId());
        if (CommunityBoardVisibilityEnum.GROUP.equals(post.getVisibility())) {
            var managerGroupIds = groupMemberRepository.findMembersByGroupRoles(post.getGroupId(), List.of(CommunityBoardGroupRoleEnum.ORGANIZER))
                .stream().map(User::getId).toList();
            if (!CollectionUtils.isEmpty(managerGroupIds)) {
                log.info("Sending flag comment notification to manager Group by comment: {}", flag.getTargetId());
                pushNotificationService.sendNotifyFlagCommentForManager(flag.getId(), flag.getReason(), managerGroupIds, portalInfo, post.getPortalId());
            }
            return;
        }

        var portalManagers = memberRepository.getAllPortalHostByPortalId(post.getPortalId());
        var adminIds = memberRepository.getAllSystemAdminId();

        // send notify for manager
        if (!CollectionUtils.isEmpty(portalManagers)) {
            log.info("Sending flag comment notification to portal host by comment: {}", flag.getTargetId());
            pushNotificationService.sendNotifyFlagCommentForManager(flag.getId(), flag.getReason(), portalManagers, portalInfo, post.getPortalId());
        }
        if (!CollectionUtils.isEmpty(adminIds)) {
            log.info("Sending flag comment notification to admin by comment: {}", flag.getTargetId());
            portalInfo = pushNotificationService.getPortalInfoForAdmin();
            pushNotificationService.sendNotifyFlagCommentForManager(flag.getId(), flag.getReason(), adminIds, portalInfo, post.getPortalId());
        }
    }

    private CommunityBoardPost getPostById(UUID id) {
        return postRepository.findById(id).orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Post")));
    }

    private CommunityBoardPost getPostByCommentId(UUID commentId) {
        return postRepository
            .findPostByCommentId(commentId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Comment")));
    }

    private CommunityBoardFlag getFlagById(UUID commentId) {
        return flagRepository
            .findById(commentId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Flag")));
    }

    private User getCurrentUser() {
        String userLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Login")));
        return userRepository
            .findOneByLogin(userLogin)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Current User")));
    }
}
