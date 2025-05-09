package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.enums.ConversationStatusEnum;
import com.formos.huub.domain.request.directmessage.*;
import com.formos.huub.domain.response.directmessage.ResponseDetailConversation;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.directmessage.DirectMessageService;
import com.formos.huub.service.pushnotification.PushNotificationService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/conversations")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DirectMessageController {

    ResponseSupport responseSupport;

    DirectMessageService directMessageService;

    PushNotificationService pushNotificationService;

    @GetMapping
    public ResponseEntity<ResponseData> getAllConversationByUser(RequestSearchConversation request) {
        var response = directMessageService.getAllConversationByUser(request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/members")
    public ResponseEntity<ResponseData> getAllMember(String searchKeyword) {
        var response = directMessageService.getAllUserInPortalByKeyword(searchKeyword);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ResponseData> getDetailConversation(@PathVariable String conversationId) {
        var response = directMessageService.getDetailConversation(UUID.fromString(conversationId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/by-direct-user/{directUserId}")
    public ResponseEntity<ResponseData> getConversationByDirectUser(@PathVariable String directUserId) {
        var response = directMessageService.getDetailConversationByDirectUser(UUID.fromString(directUserId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PostMapping
    public ResponseEntity<ResponseData> createConversation(@RequestBody @Valid RequestCreateConversation request) {
        var response = directMessageService.createNewConversation(request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PostMapping("/send-message")
    public ResponseEntity<ResponseData> sendMessage(@RequestBody @Valid RequestSendMessageToConversation request) {
        directMessageService.sendMessageToConversation(request);
        return responseSupport.success(ResponseData.builder().build());
    }

    @PostMapping("/send-message-all")
    @PreAuthorize("hasPermission(null, 'SEND_MESSAGE_ALL')")
    public ResponseEntity<ResponseData> sendMessageAll(@RequestBody @Valid RequestSendMessageAll request) {
        ResponseDetailConversation response = directMessageService.sendMessageAll(request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PostMapping("/referral-message/response")
    public ResponseEntity<ResponseData> referralMessageResponse(@RequestBody @Valid RequestSendResponseReferralMessage request) {
        directMessageService.sendResponseReferralMessage(request);
        return responseSupport.success(ResponseData.builder().build());
    }

    @PostMapping("/mentions")
    public ResponseEntity<ResponseData> mentionInConversation(@RequestBody @Valid RequestMentionedUser request) {
        pushNotificationService.sendMentionNotification(request.getUserIds(), request.getConversationId());
        return responseSupport.success(ResponseData.builder().build());
    }

    @PatchMapping("/{conversationId}/archive")
    public ResponseEntity<ResponseData> archiveConversation(@PathVariable String conversationId) {
        directMessageService.archiveMessage(UUID.fromString(conversationId), ConversationStatusEnum.ARCHIVED);
        return responseSupport.success(ResponseData.builder().build());
    }

    @PatchMapping("/{conversationId}/un-archive")
    public ResponseEntity<ResponseData> unArchiveConversation(@PathVariable String conversationId) {
        directMessageService.archiveMessage(UUID.fromString(conversationId), ConversationStatusEnum.ACTIVE);
        return responseSupport.success(ResponseData.builder().build());
    }

    @PatchMapping("/{blockedId}/block")
    public ResponseEntity<ResponseData> blockUser(@PathVariable String blockedId) {
        var response = directMessageService.blockMember(UUID.fromString(blockedId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PatchMapping("/{blockedId}/un-block")
    public ResponseEntity<ResponseData> unBlockUser(@PathVariable String blockedId) {
        var response = directMessageService.unBlockMember(UUID.fromString(blockedId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData> deleteConversation(@PathVariable String id) {
        directMessageService.deleteConversation(UUID.fromString(id));
        return responseSupport.success(ResponseData.builder().build());
    }

    @PostMapping("/notification/new-message")
    public ResponseEntity<ResponseData> notificationSendMessage(@RequestBody @Valid RequestNotificationSendMessage request) {
        pushNotificationService.sendNotifyToUserOfNewMessage(request.getUserIds(), request.getConversationId());
        return responseSupport.success(ResponseData.builder().build());
    }

    @GetMapping("/check-exist/{blockedId}")
    public ResponseEntity<ResponseData> checkExistingBlocker(@PathVariable String blockedId) {
;        var response = directMessageService.existsByBlockerIdAndBlockedId(UUID.fromString(blockedId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }
}
