package com.formos.huub.domain.response.technicalassistance;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseInfoAdvisor {

    private UUID userId;

    private UUID technicalAdvisorId;
    
    private String fullName;

    private String imageUrl;
}
