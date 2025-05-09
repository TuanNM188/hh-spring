package com.formos.huub.service.communityboard;

import com.formos.huub.domain.entity.CommunityBoardLike;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.enums.CommunityBoardEntryTypeEnum;
import com.formos.huub.domain.request.communityboardlike.RequestAddLikeCommunityBoardComment;
import com.formos.huub.domain.request.communityboardlike.RequestAddLikeCommunityBoardPost;
import com.formos.huub.domain.response.communityboardreaction.IResponseCommunityBoardReaction;
import com.formos.huub.domain.response.communityboardreaction.ResponseCommunityBoardReaction;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.mapper.member.MemberMapper;
import com.formos.huub.repository.CommunityBoardGroupRepository;
import com.formos.huub.repository.CommunityBoardLikeRepository;
import com.formos.huub.repository.UserRepository;
import com.formos.huub.security.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommunityBoardLikeService extends BaseService {

    CommunityBoardLikeRepository likeRepository;
    CommunityBoardGroupRepository groupRepository;
    UserRepository userRepository;
    MemberMapper memberMapper;

    public List<IResponseCommunityBoardReaction> getListReaction(UUID entryId) {
        return likeRepository.findAllByEntryId(entryId);
    }

    public ResponseCommunityBoardReaction addOrRemoveLikeToPost(RequestAddLikeCommunityBoardPost request) {
        var postId = UUIDUtils.convertToUUID(request.getPostId());
        var user = getCurrentUser();
        var reactions = likeRepository.findByAuthorIdAndEntryIdAndEntryType(user.getId(), postId, CommunityBoardEntryTypeEnum.POST);
        ResponseCommunityBoardReaction result = null;
        if (reactions.isEmpty()) {
            result = addLikeToPost(request, user);
        } else {
            removeLikeByEntity(reactions);
        }
        groupRepository.findGroupIdByPostId(postId)
            .ifPresent(groupId -> groupRepository.updateLastActive(groupId, Instant.now()));
        return result;
    }

    public ResponseCommunityBoardReaction addOrRemoveLikeToComment(RequestAddLikeCommunityBoardComment request) {
        var commentId = UUIDUtils.convertToUUID(request.getCommentId());
        var user = getCurrentUser();
        var reactions = likeRepository.findByAuthorIdAndEntryIdAndEntryType(user.getId(), commentId, CommunityBoardEntryTypeEnum.COMMENT);
        ResponseCommunityBoardReaction result = null;
        if (reactions.isEmpty()) {
            result = addLikeToComment(request, user);
        } else {
            removeLikeByEntity(reactions);
        }
        groupRepository.findGroupIdByCommentId(commentId)
            .ifPresent(groupId -> groupRepository.updateLastActive(groupId, Instant.now()));
        return result;
    }
    private ResponseCommunityBoardReaction addLikeToComment(RequestAddLikeCommunityBoardComment request, User user) {
        var commentId = UUIDUtils.convertToUUID(request.getCommentId());
        var reaction = CommunityBoardLike
            .builder()
            .likeIcon(request.getLikeIcon())
            .entryType(CommunityBoardEntryTypeEnum.COMMENT)
            .entryId(commentId)
            .authorId(user.getId())
            .build();
        likeRepository.save(reaction);
        return ResponseCommunityBoardReaction
            .builder()
            .id(reaction.getId())
            .author(memberMapper.toResponseCommunityBoard(user))
            .build();
    }

    private ResponseCommunityBoardReaction addLikeToPost(RequestAddLikeCommunityBoardPost request, User user) {
        var postId = UUIDUtils.convertToUUID(request.getPostId());
        var reaction = CommunityBoardLike
            .builder()
            .likeIcon(request.getLikeIcon())
            .entryType(CommunityBoardEntryTypeEnum.POST)
            .entryId(postId)
            .authorId(user.getId())
            .build();
        likeRepository.save(reaction);
        return ResponseCommunityBoardReaction
            .builder()
            .id(reaction.getId())
            .author(memberMapper.toResponseCommunityBoard(user))
            .build();
    }

    private void removeLikeByEntity(List<CommunityBoardLike> likes) {
        likeRepository.deleteAll(likes);
    }

    private User getCurrentUser() {
        String userLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Login")));
        return userRepository
            .findOneByLogin(userLogin)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Current User")));
    }

}
