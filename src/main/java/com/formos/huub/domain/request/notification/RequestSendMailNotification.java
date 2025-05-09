package com.formos.huub.domain.request.notification;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestSendMailNotification {
    String email;
    String senderName;
    String receiverName;
    String referenceUrl;
    String portalName;
    String templatePath;
    String titleKey;
    String description;
}
