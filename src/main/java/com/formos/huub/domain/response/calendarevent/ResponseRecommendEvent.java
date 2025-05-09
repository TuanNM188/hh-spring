package com.formos.huub.domain.response.calendarevent;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseRecommendEvent {

    private UUID id;

    private String title;

    private Instant startDate;

    private Instant endDate;

    private String imageUrl;
}
