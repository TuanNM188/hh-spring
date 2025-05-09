package com.formos.huub.domain.response.businessowner;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseSearchCourseRegistrations {

    private UUID id;
    private String name;
    private String accessType;
    private UUID categoryId;
    private String categoryName;
    private Instant enrolledDate;
    private Instant startedDate;
    private Instant completedDate;
    private Integer rating;
    private Integer completedLesson;
    private Integer totalLesson;
}
