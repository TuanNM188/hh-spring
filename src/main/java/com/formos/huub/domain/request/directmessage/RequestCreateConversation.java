package com.formos.huub.domain.request.directmessage;

import com.formos.huub.domain.enums.ConversationTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestCreateConversation {

    private String name;

    private List<UUID> userIds;

    @NotNull
    private ConversationTypeEnum conversationType;
}
