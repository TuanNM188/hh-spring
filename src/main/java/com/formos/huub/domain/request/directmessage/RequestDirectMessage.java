package com.formos.huub.domain.request.directmessage;

import com.formos.huub.domain.enums.MessageTypeEnum;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestDirectMessage {

    private String senderId;

    private String content;

    private MessageTypeEnum messageType;

    private String url;

    private Boolean isPin;

    private Long sendAt;

    private String status;

    private List<ReadReceipt> readReceipts;

    private List<Reaction> reactions;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadReceipt{
        private String userId;
        private Long readAt;
    }

    @Getter
    @Setter
    public static class Reaction{
        private String userId;
        private String type;
    }
}
