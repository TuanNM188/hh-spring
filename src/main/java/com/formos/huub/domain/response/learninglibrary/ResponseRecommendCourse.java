package com.formos.huub.domain.response.learninglibrary;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseRecommendCourse {

    private UUID id;

    private String name;

    private String categoryName;

    private String imageUrl;

    private Instant startDate;
}
