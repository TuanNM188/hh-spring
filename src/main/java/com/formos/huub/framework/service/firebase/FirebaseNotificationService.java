package com.formos.huub.framework.service.firebase;

import com.formos.huub.domain.enums.ModifyTypeEnum;
import com.formos.huub.domain.request.directmessage.RequestDirectMessage;
import com.formos.huub.domain.request.directmessage.RequestNewConversationFirebase;
import com.formos.huub.domain.request.directmessage.RequestParticipant;
import com.formos.huub.domain.response.notification.NotificationData;
import com.formos.huub.framework.properties.FirebaseProperties;
import com.google.api.core.ApiFuture;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class FirebaseNotificationService {

    private static final String NOTIFICATIONS_URI = "/notifications";
    private static final String DIRECT_MESSAGE_URI = "/conversations";
    private static final String NEW_DIRECT_MESSAGE_URI = "/new-conversations";

    FirebaseProperties firebaseProperties;

    /**
     * Send notification to real-time database
     *
     * @param userIds List<UUID>
     * @param data    NotificationData
     */
    public void sendNotificationToRealTimeDb(List<UUID> userIds, NotificationData data) {
        if (userIds == null || userIds.isEmpty()) {
            log.warn("User IDs list is empty. No notifications to send.");
            return;
        }
        for (UUID userId : userIds) {
            sendNotificationToUser(userId, data);
        }
    }

    /**
     * Send message to conversation
     *
     * @param conversationId UUID
     * @param data           RequestDirectMessage
     */
    public void sendMessageToConversation(UUID conversationId, RequestDirectMessage data) {
        sendDataToFirebase(DIRECT_MESSAGE_URI, conversationId, "messages", data);
    }

    /**
     * Add multiple participants to conversation
     *
     * @param conversationId UUID
     * @param data           List<RequestParticipant>
     */
    public void addMultiParticipantsToConversation(UUID conversationId, List<RequestParticipant> data) {
        data.forEach(participant -> addParticipantToConversation(conversationId, participant));
    }

    public void sendNotificationNewConversation(UUID conversationId, List<RequestParticipant> data, ModifyTypeEnum modifyType) {
        long timestamp = Instant.now().toEpochMilli();
        var requestNewConversation = RequestNewConversationFirebase.builder()
            .lastUpdated(timestamp)
            .participants(data)
            .modifyType(modifyType.getValue())
            .conversationId(conversationId.toString())
            .build();
        pushObjectWithCustomKey(NEW_DIRECT_MESSAGE_URI, conversationId.toString(), requestNewConversation);
    }

    /**
     * Add participant to conversation
     *
     * @param conversationId UUID
     * @param data           RequestParticipant
     */
    public void addParticipantToConversation(UUID conversationId, RequestParticipant data) {
        sendDataToFirebase(DIRECT_MESSAGE_URI, conversationId, "participants", data);
    }

    /**
     * Send notification to user
     *
     * @param userId UUID
     * @param data   NotificationData
     */
    private void sendNotificationToUser(UUID userId, NotificationData data) {
        sendDataToFirebase(NOTIFICATIONS_URI, userId, null, data);
    }

    /**
     * Send data to Firebase
     *
     * @param baseUri   String
     * @param id        UUID
     * @param childNode String
     * @param data      Object
     */
    private void sendDataToFirebase(String baseUri, UUID id, String childNode, Object data) {
        try {
            DatabaseReference messageRef = FirebaseDatabase.getInstance(firebaseProperties.getRealTimeDatabaseUrl())
                .getReference(baseUri)
                .child(id.toString());

            if (childNode != null) {
                messageRef = messageRef.child(childNode);
            }
            DatabaseReference pushRef = messageRef.push();

            pushRef.setValue(data, (error, ref) -> {
                if (error != null) {
                    log.error("Data could not be saved for {}: {}", id, error.getMessage());
                } else {
                    log.info("Data saved successfully for {}", id);
                }
            });
        } catch (Exception e) {
            log.error("Failed to send data to Firebase for {}: {}", id, e.getMessage());
        }
    }

    private void pushObjectWithCustomKey(String path, String customKey, Object object) {
        // Get a reference to the database
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

        // Reference the custom path
        DatabaseReference customRef = databaseRef.child(path).child(customKey);

        // Set the object at the custom key
        ApiFuture<Void> future = customRef.setValueAsync(object);
        try {
            // Wait for the operation to complete
            future.get();
            System.out.println("Object pushed successfully with custom key: " + customKey);
        } catch (Exception e) {
            // Handle exceptions during the operation
            System.err.println("Failed to push object: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateFieldToFirebase(String baseUri, UUID id, String fieldKey, Object data) {
        try {
            // Get a reference to the database
            DatabaseReference messageRef = FirebaseDatabase.getInstance(firebaseProperties.getRealTimeDatabaseUrl()).getReference(baseUri);
            if (id != null) {
                messageRef = messageRef.child(id.toString());
            }
            // Add or update the new field
            messageRef
                .child(fieldKey)
                .setValue(data, (error, ref) -> {
                    if (error != null) {
                        log.error("Data could not be saved for {}: {}", id, error.getMessage());
                    } else {
                        log.info("Data saved successfully for {}", id);
                    }
                });
        } catch (Exception e) {
            log.error("Failed to send data to Firebase for {}: {}", id, e.getMessage());
        }
    }
}
