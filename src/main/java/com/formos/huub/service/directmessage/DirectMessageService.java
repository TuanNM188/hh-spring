package com.formos.huub.service.directmessage;

import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.*;
import com.formos.huub.domain.request.directmessage.*;
import com.formos.huub.domain.response.directmessage.*;
import com.formos.huub.domain.response.member.ResponseMetaMember;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.service.firebase.FirebaseNotificationService;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.repository.*;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.pushnotification.PushNotificationService;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class DirectMessageService {

    private static final Gson GSON = new Gson();
    private static final Type LIST_TYPE = new TypeToken<ArrayList<ResponseConversationUser>>() {
    }.getType();
    private static final int MAX_CONVERSATION_NAME_LENGTH = 2000;

    ConversationRepository conversationRepository;

    ConversationUserRepository conversationUserRepository;

    UserRepository userRepository;

    BlockedUserRepository blockedUserRepository;

    MemberRepository memberRepository;

    FirebaseNotificationService firebaseNotificationService;

    ReferralMessageRepository referralMessageRepository;

    CommunityPartnerRepository communityPartnerRepository;

    BusinessOwnerRepository businessOwnerRepository;

    PushNotificationService pushNotificationService;

    public List<ResponseConversation> getAllConversationByUser(RequestSearchConversation request) {
        var currentUser = getCurrentUser();
        var userId = currentUser.getId();

        return conversationRepository
            .searchConversationByCondition(request, userId)
            .stream()
            .map(conversation -> buildResponseConversation(conversation, currentUser.getId()))
            .toList();
    }

    public ResponseDetailConversation getDetailConversation(UUID conversationId) {
        var currentUser = getCurrentUser();
        var conversation = getConversationById(currentUser.getId(), conversationId);

        return buildDetailConversationResponse(conversation, currentUser);
    }

    public ResponseDetailConversation getDetailConversationByDirectUser(UUID directUserId) {
        var currentUser = getCurrentUser();
        var conversation = getConversationByDirectUser(currentUser.getId(), directUserId);
        if (Objects.isNull(conversation.getId())) {
            var response = new ResponseDetailConversation();
            userRepository
                .findById(directUserId)
                .ifPresent(directUser -> {
                    response.setDirectUserName(directUser.getNormalizedFullName());
                    response.setDirectUserImageUrl(directUser.getImageUrl());
                });
            return response;
        }
        return buildDetailConversationResponse(conversation, currentUser);
    }

    private ResponseDetailConversation buildDetailConversationResponse(ResponseConversation conversation, User currentUser) {
        var response = new ResponseDetailConversation();
        BeanUtils.copyProperties(conversation, response);

        if (isSingleConversation(conversation)) {
            findBlockedUserInfo(conversation, currentUser).ifPresent(blockedUserInfo -> {
                response.setIsBlocked(true);
                response.setBlockerId(blockedUserInfo.getBlockerId());
            });
            if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.COMMUNITY_PARTNER)) {
                referralMessageRepository.findByConversationIdByReferralMessage(conversation.getId())
                    .stream().findFirst()
                    .ifPresent(ele -> response.setIsRequireResponseReferralMessage(!ele.getIsResponse()));
            }
        }
        return response;
    }

    private boolean isBlockedUser(UUID currentUserId, UUID blockedUserId) {
        AtomicReference<Boolean> isBlocked = new AtomicReference<>(false);
        getBlockedUserInfo(currentUserId, blockedUserId).ifPresent(blockedUserInfo -> isBlocked.set(true));
        return isBlocked.get();
    }

    public List<ResponseMetaMember> getAllUserInPortalByKeyword(String searchKeyword) {
        UUID contextPortalId = PortalContextHolder.getPortalId();
        return memberRepository
            .getAllUserInPortalByKeyword(contextPortalId, searchKeyword)
            .stream()
            .map(this::buildResponseMetaMember)
            .toList();
    }

    public void archiveMessage(UUID conversationId, ConversationStatusEnum status) {
        var currentUser = getCurrentUser();
        var conversationUser = conversationUserRepository
            .findByConversationIdAndUserId(conversationId, currentUser.getId())
            .stream()
            .findFirst()
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Conversation")));
        conversationUser.setStatus(status);
        conversationUserRepository.save(conversationUser);
    }

    public void deleteConversation(UUID conversationId) {
        var response = getDetailConversation(conversationId);
        var participantsFirebase = response
            .getConversationUsers()
            .stream()
            .map(ele -> RequestParticipant.builder().userId(ele.getUserId().toString()).build())
            .toList();
        conversationUserRepository.deleteAllByConversationId(conversationId);
        conversationRepository.deleteById(conversationId);
        firebaseNotificationService.sendNotificationNewConversation(conversationId, participantsFirebase, ModifyTypeEnum.DELETE);
    }

    public UUID blockMember(UUID blockedId) {
        var currentUser = getCurrentUser();
        var blockedUer = userRepository
            .findById(blockedId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Blocked User")));
        if (blockedUserRepository.existsByBlockerIdAndBlockedId(currentUser.getId(), blockedId)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "User already blocked."));
        }
        BlockedUser blockedUser = BlockedUser.builder().blocker(currentUser).blocked(blockedUer).build();
        blockedUserRepository.save(blockedUser);
        var conversation = getConversationByDirectUser(currentUser.getId(), blockedUer.getId());
        if (Objects.nonNull(conversation)) {
            return conversation.getId();
        }
        return null;
    }

    public UUID unBlockMember(UUID blockedId) {
        var currentUser = getCurrentUser();
        blockedUserRepository.deleteByBlockerIdAndBlockedId(currentUser.getId(), blockedId);
        var conversation = getConversationByDirectUser(currentUser.getId(), blockedId);
        if (Objects.nonNull(conversation)) {
            return conversation.getId();
        }
        return null;
    }

    public ResponseDetailConversation createNewConversation(RequestCreateConversation request) {
        var currentUser = getCurrentUser();
        var portalId = PortalContextHolder.getPortalId();
        List<User> participants = determineParticipants(request);

        if (ObjectUtils.isEmpty(participants)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Participant"));
        }

        // Check existing single conversation
        var participant = participants.stream().filter(ele -> !ele.getId().equals(currentUser.getId())).toList().getFirst();
        if (isBlockedUser(currentUser.getId(), participant.getId())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0051));
        }

        if (checkExistConversationSingle(request, currentUser.getId(), participant.getId(), portalId)) {
            return getExistingConversation(currentUser.getId(), participant.getId());
        }

        // Create new conversation
        boolean existCurrentUser = !participants.stream().filter(ele -> ele.getId().equals(currentUser.getId())).toList().isEmpty();
        if (!existCurrentUser) {
            participants.add(currentUser);
        }

        var conversation = initializeAndSaveConversation(request.getConversationType());
        saveConversationUsers(conversation, participants);
        updateNameConversation(conversation);
        var response = getDetailConversation(conversation.getId());

        // Add participants to Firebase
        addParticipantsToFirebase(response);
        return response;
    }

    private void addParticipantsToFirebase(ResponseDetailConversation response) {
        var currentTime = Timestamp.from(Instant.now()).getTime();
        var participantsFirebase = response
            .getConversationUsers()
            .stream()
            .map(ele -> RequestParticipant.builder().joinAt(currentTime).userId(ele.getUserId().toString()).build())
            .toList();
        firebaseNotificationService.addMultiParticipantsToConversation(response.getId(), participantsFirebase);
        firebaseNotificationService.sendNotificationNewConversation(response.getId(), participantsFirebase, ModifyTypeEnum.NEW);
    }

    public void sendMessageToConversation(RequestSendMessageToConversation request) {
        var conversation = findConversation(request.getUserIds());
        User senderUser = (request.getSenderId() != null)
            ? getUserById(request.getSenderId())
            : getCurrentUser();

        String senderIdStr = senderUser.getId().toString();
        var currentTime = Timestamp.from(Instant.now()).getTime();

        var message = buildDirectMessage(
            request.getMessage(),
            senderIdStr,
            currentTime,
            MessageTypeEnum.TEXT
        );

        List<UUID> recipientIds = request.getUserIds().stream()
            .filter(id -> !id.toString().equals(senderIdStr))
            .toList();

        firebaseNotificationService.sendMessageToConversation(conversation.getId(), message);
        pushNotificationService.sendNotifyToUserOfNewMessage(recipientIds, conversation.getId(), senderUser);
        saveReferralMessage(conversation.getId(), request);
    }

    /**
     * Send message all
     *
     * @param request RequestSendMessageAll
     * @return ResponseDetailConversation
     */
    public ResponseDetailConversation sendMessageAll(RequestSendMessageAll request) {
        UUID senderId = getCurrentUser().getId();
        long currentTime = Timestamp.from(Instant.now()).getTime();
        UUID portalId = PortalContextHolder.getPortalId();
        List<UUID> portalIds = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.SYSTEM_ADMINISTRATOR)
            ? request.getPortalIds() : Collections.singletonList(portalId);

        RequestDirectMessage message = buildDirectMessage(request.getMessage(), String.valueOf(senderId), currentTime, request.getMessageType());
        List<User> users = getAllUserInPortal(portalIds);

        if (ObjectUtils.isEmpty(users)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "User"));
        }

        List<UUID> userIds = users.stream().map(User::getId).filter(id -> !id.equals(senderId))
            .toList();

        ResponseDetailConversation firstConversation = null;
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        for (UUID userId : userIds) {
            if (existsByBlockerIdAndBlockedId(userId)) {
                continue;
            }

            ResponseDetailConversation conversation = findConversation(Collections.singletonList(userId));
            UUID conversationId = conversation.getId();

            firebaseNotificationService.sendMessageToConversation(conversationId, message);
            pushNotificationService.sendNotifyToUserOfNewMessage(Collections.singletonList(userId), conversationId, currentUser);

            if (Objects.isNull(firstConversation)) {
                firstConversation = conversation;
            }
        }

        return firstConversation;
    }

    public void sendResponseReferralMessage(RequestSendResponseReferralMessage request) {
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.COMMUNITY_PARTNER)) {
            return;
        }
        var conversation = conversationRepository.findById(request.getConversationId())
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Conversation")));

        var referralMessage = referralMessageRepository.findByConversationId(conversation.getId()).orElse(null);
        if (Objects.isNull(referralMessage) || Boolean.TRUE.equals(referralMessage.getIsResponse())) {
            return;
        }
        referralMessage.setIsResponse(true);
        referralMessage.setMessageResponse(request.getMessage());
        referralMessage.setResponseAt(Instant.now());
        referralMessageRepository.save(referralMessage);
    }


    public boolean checkMemberForPortal(String portalId) {
        var responseMember = getAllUserInPortal(Collections.singletonList(UUID.fromString(portalId)));
        return !ObjectUtils.isEmpty(responseMember);
    }

    public boolean existsByBlockerIdAndBlockedId(UUID blockedId) {
        return blockedUserRepository.existsByBlocker(getCurrentUser().getId(), blockedId);
    }

    private void saveReferralMessage(UUID conversationId, RequestSendMessageToConversation request) {
        if (!isValidReferralRequest(request)) {
            return;
        }
        conversationRepository.findById(conversationId).ifPresent(conversation ->
            referralMessageRepository
                .findByCommunityPartnerIdAndConversationId(request.getCommunityPartnerId(), conversationId)
                .ifPresentOrElse(
                    referralMessage -> updateReferralMessage(referralMessage, request.getMessage()),
                    () -> createReferralMessage(request, conversation)
                )
        );
    }

    private boolean isValidReferralRequest(RequestSendMessageToConversation request) {
        return SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.BUSINESS_OWNER)
            && Objects.nonNull(request.getCommunityPartnerId());
    }

    private void updateReferralMessage(ReferralMessage referralMessage, String message) {
        referralMessage.setSendAt(Instant.now());
        referralMessage.setMessage(message);
        referralMessage.setMessageResponse(null);
        referralMessage.setIsResponse(false);
        referralMessageRepository.save(referralMessage);
    }

    private void createReferralMessage(RequestSendMessageToConversation request, Conversation conversation) {
        var communityPartner = communityPartnerRepository.findById(request.getCommunityPartnerId()).orElse(null);
        if (communityPartner == null) {
            return;
        }
        var primaryCommunityPartner = getCommunityPartnerPrimaryContactId(communityPartner.getId());
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        var businessOwner = businessOwnerRepository.findByUserId(currentUser.getId())
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010,
                "Business Owner")));
        var referralMessage = ReferralMessage.builder()
            .isResponse(false)
            .message(request.getMessage())
            .conversation(conversation)
            .communityPartner(communityPartner)
            .sendAt(Instant.now())
            .primaryUser(primaryCommunityPartner)
            .businessOwner(businessOwner)
            .build();

        referralMessageRepository.save(referralMessage);
    }

    public User getCommunityPartnerPrimaryContactId(UUID communityPartnerId) {
        return userRepository.getCommunityPartnerPrimaryById(communityPartnerId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010,
                "Primary Community Partner")));
    }

    private RequestDirectMessage buildDirectMessage(String message, String currentUserId, long currentTime, MessageTypeEnum messageType) {
        return RequestDirectMessage.builder()
            .messageType(messageType)
            .content(message)
            .sendAt(currentTime)
            .senderId(currentUserId)
            .isPin(false)
            .readReceipts(List.of(RequestDirectMessage.ReadReceipt.builder().userId(currentUserId).readAt(currentTime).build()))
            .build();
    }

    private ResponseMetaMember buildResponseMetaMember(User user) {
        return ResponseMetaMember.builder().userId(user.getId()).name(user.getNormalizedFullName()).imageUrl(user.getImageUrl()).build();
    }

    private boolean isSingleConversation(ResponseConversation conversation) {
        return ConversationTypeEnum.SINGLE.equals(conversation.getConversationType());
    }

    private Optional<BlockedUserInfo> findBlockedUserInfo(ResponseConversation conversation, User currentUser) {
        return conversation
            .getConversationUsers()
            .stream()
            .filter(user -> !user.getUserId().equals(currentUser.getId()))
            .findFirst()
            .flatMap(blockedUser -> getBlockedUserInfo(currentUser.getId(), blockedUser.getUserId()));
    }

    private Optional<BlockedUserInfo> getBlockedUserInfo(UUID currentUserId, UUID otherUserId) {
        return blockedUserRepository
            .findByBlockerIdAndBlockedId(currentUserId, otherUserId)
            .or(() -> blockedUserRepository.findByBlockerIdAndBlockedId(otherUserId, currentUserId))
            .map(blockUser -> new BlockedUserInfo(blockUser.getBlocker().getId()));
    }

    private ResponseDetailConversation findConversation(List<UUID> userIds) {
        var requestFindConversation = RequestCreateConversation.builder()
            .userIds(userIds)
            .conversationType(ConversationTypeEnum.SINGLE)
            .build();
        return createNewConversation(requestFindConversation);
    }

    private ResponseConversation buildResponseConversation(IResponseListConversation conversation, UUID userId) {
        var responseConversation = new ResponseConversation();
        BeanUtils.copyProperties(conversation, responseConversation);

        ArrayList<ResponseConversationUser> participants = GSON.fromJson(conversation.getUserInConversation(), LIST_TYPE);
        participants
            .stream()
            .filter(
                participant ->
                    !participant.getUserId().equals(userId) && ConversationTypeEnum.SINGLE.equals(conversation.getConversationType())
            )
            .findFirst()
            .ifPresent(participant -> {
                responseConversation.setName(participant.getName());
                responseConversation.setImageUrl(participant.getImageUrl());
            });
        responseConversation.setConversationUsers(participants);
        return responseConversation;
    }

    private List<User> determineParticipants(RequestCreateConversation request) {
        if (ObjectUtils.isEmpty(request.getUserIds())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0027, "UserIds"));
        }
        var userIds = request.getUserIds().stream().distinct().toList();
        return userRepository.findAllById(userIds);
    }

    private Conversation initializeAndSaveConversation(ConversationTypeEnum conversationType) {
        ConversationMessageTypeEnum conversationMessageType = ConversationMessageTypeEnum.DIRECT_MESSAGE;
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.SYSTEM_ADMINISTRATOR)) {
            conversationMessageType = ConversationMessageTypeEnum.SYSTEM_MESSAGE;
        }
        var portalId = PortalContextHolder.getPortalId();
        var conversation = Conversation.builder()
            .conversationType(conversationType)
            .status(ConversationStatusEnum.ACTIVE)
            .portalId(portalId)
            .conversationMessageType(conversationMessageType)
            .build();
        return conversationRepository.save(conversation);
    }

    private void saveConversationUsers(Conversation conversation, List<User> participants) {
        var conversationUsers = participants
            .stream()
            .map(
                user ->
                    ConversationUser.builder()
                        .nickname(user.getNormalizedFullName())
                        .conversation(conversation)
                        .user(user)
                        .status(ConversationStatusEnum.ACTIVE)
                        .build()
            )
            .toList();
        conversationUserRepository.saveAll(conversationUsers);
    }

    private void updateNameConversation(Conversation conversation) {
        var nameConversation = conversationUserRepository.getNameConversation(conversation.getId());
        if (nameConversation.length() > MAX_CONVERSATION_NAME_LENGTH) {
            nameConversation = nameConversation.substring(0, MAX_CONVERSATION_NAME_LENGTH);
        }
        conversation.setName(nameConversation);
        conversationRepository.save(conversation);
    }

    private boolean checkExistConversationSingle(RequestCreateConversation request, UUID userId, UUID directUserId, UUID portalId) {
        if (ConversationTypeEnum.GROUP.equals(request.getConversationType())) {
            return false;
        }
        var requestSearchConversation = RequestSearchConversation.builder()
            .directUserId(directUserId.toString())
            .conversationType(ConversationTypeEnum.SINGLE.toString())
            .portalId(portalId)
            .conversationMessageType(String.valueOf(ConversationMessageTypeEnum.DIRECT_MESSAGE))
            .build();

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.SYSTEM_ADMINISTRATOR)) {
            requestSearchConversation.setConversationMessageType(String.valueOf(ConversationMessageTypeEnum.SYSTEM_MESSAGE));
        }

        return conversationRepository.searchConversationByCondition(requestSearchConversation, userId).stream().findFirst().isPresent();
    }

    private ResponseConversation getConversationById(UUID userId, UUID conversationId) {
        var request = RequestSearchConversation.builder().conversationId(conversationId).build();
        return getConversation(userId, request);
    }

    private ResponseConversation getConversationByDirectUser(UUID userId, UUID directUserId) {
        var request = RequestSearchConversation.builder()
            .conversationType(ConversationTypeEnum.SINGLE.toString())
            .directUserId(directUserId.toString())
            .conversationMessageType(String.valueOf(ConversationMessageTypeEnum.DIRECT_MESSAGE))
            .build();
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.SYSTEM_ADMINISTRATOR)) {
            request.setConversationMessageType(String.valueOf(ConversationMessageTypeEnum.SYSTEM_MESSAGE));
        }
        return getConversation(userId, request);
    }

    private ResponseDetailConversation getExistingConversation(UUID userId, UUID directUserId) {
        var conversation = getConversationByDirectUser(userId, directUserId);
        var response = new ResponseDetailConversation();
        BeanUtils.copyProperties(conversation, response);
        response.setIsNew(false);
        return response;
    }

    private ResponseConversation getConversation(UUID userId, RequestSearchConversation request) {
        return conversationRepository
            .searchConversationByCondition(request, userId)
            .stream()
            .findFirst()
            .map(conversation -> buildResponseConversation(conversation, userId))
            .orElse(new ResponseConversation());
    }

    private List<User> getAllUserInPortal(List<UUID> portalIds) {
        validateNotifyAll();

        List<UUID> portalIdList = ObjectUtils.isEmpty(portalIds)
            ? Collections.singletonList(PortalContextHolder.getPortalId())
            : portalIds;

        return memberRepository.getAllUserInPortal(portalIdList);
    }

    private void validateNotifyAll() {
        if (!SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.SYSTEM_ADMINISTRATOR, AuthoritiesConstants.PORTAL_HOST)) {
            throw new AccessDeniedException("Role Member");
        }
    }

    /**
     * Get current User
     *
     * @return User
     */
    private User getCurrentUser() {
        return SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneWithAuthoritiesByLogin)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "User")));
    }

    private User getUserById(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "User")));
    }
}
