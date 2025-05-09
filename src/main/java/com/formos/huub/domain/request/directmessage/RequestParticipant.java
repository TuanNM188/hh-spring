package com.formos.huub.domain.request.directmessage;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestParticipant {

    private String userId;

    private Long joinAt;
}
